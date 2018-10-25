package cz.iocb.chemweb.server.sparql.translator.imcode;

import static cz.iocb.chemweb.server.sparql.mapping.classes.BuiltinClasses.rdfLangString;
import static cz.iocb.chemweb.server.sparql.mapping.classes.BuiltinClasses.xsdDate;
import static cz.iocb.chemweb.server.sparql.mapping.classes.BuiltinClasses.xsdDateTime;
import cz.iocb.chemweb.server.sparql.mapping.classes.DateConstantZoneClass;
import cz.iocb.chemweb.server.sparql.mapping.classes.DateTimeConstantZoneClass;
import cz.iocb.chemweb.server.sparql.mapping.classes.LangStringConstantTagClass;
import cz.iocb.chemweb.server.sparql.mapping.classes.ResourceClass;
import cz.iocb.chemweb.server.sparql.mapping.classes.UserIriClass;
import cz.iocb.chemweb.server.sparql.translator.UsedVariable;
import cz.iocb.chemweb.server.sparql.translator.UsedVariables;
import cz.iocb.chemweb.server.sparql.translator.imcode.expression.SqlExpressionIntercode;
import cz.iocb.chemweb.server.sparql.translator.imcode.expression.SqlNodeValue;
import cz.iocb.chemweb.server.sparql.translator.imcode.expression.SqlNull;



public class SqlBind extends SqlIntercode
{
    String variableName;
    SqlExpressionIntercode expression;
    SqlIntercode context;


    protected SqlBind(UsedVariables variables, String variableName, SqlExpressionIntercode expression,
            SqlIntercode context)
    {
        super(variables);
        this.variableName = variableName;
        this.expression = expression;
        this.context = context;
    }


    public static SqlIntercode bind(String variable, SqlExpressionIntercode expression, SqlIntercode context)
    {
        if(context instanceof SqlNoSolution)
            return new SqlNoSolution();

        if(expression == SqlNull.get())
            return context;

        UsedVariables variables = new UsedVariables();

        for(UsedVariable var : context.variables.getValues())
            variables.add(var);

        variables.add(new UsedVariable(variable, expression.getResourceClasses(), expression.canBeNull()));

        return new SqlBind(variables, variable, expression, context);
    }


    @Override
    public String translate()
    {
        boolean useTwoPhases = true;
        StringBuilder builder = new StringBuilder();

        if(expression instanceof SqlNodeValue)
        {
            useTwoPhases = false;
            builder.append("SELECT ");

            SqlNodeValue node = (SqlNodeValue) expression;
            boolean hasSelect = false;

            for(ResourceClass resourceClass : node.getResourceClasses())
            {
                for(int i = 0; i < resourceClass.getPatternPartsCount(); i++)
                {
                    appendComma(builder, hasSelect);
                    hasSelect = true;

                    builder.append(node.getNodeAccess(resourceClass, i));
                    builder.append(" AS ");
                    builder.append(resourceClass.getSqlColumn(variableName, i));
                }
            }
        }
        else
        {
            String columnName = '"' + variableName + "#expression\"";

            if(expression.isBoxed() == false && expression.getResourceClasses().size() == 1)
            {
                ResourceClass resourceClass = expression.getResourceClasses().iterator().next();

                if(!(resourceClass instanceof UserIriClass) && resourceClass.getPatternPartsCount() == 1)
                {
                    columnName = resourceClass.getSqlColumn(variableName, 0);
                    useTwoPhases = false;
                }
            }


            if(useTwoPhases)
            {
                boolean splitDateTimeClasses = expression.getResourceClasses().stream()
                        .filter(r -> r instanceof DateTimeConstantZoneClass).count() > 1;

                boolean splitDateClasses = expression.getResourceClasses().stream()
                        .filter(r -> r instanceof DateConstantZoneClass).count() > 1;

                boolean splitLangClasses = expression.getResourceClasses().stream()
                        .filter(r -> r instanceof LangStringConstantTagClass).count() > 1;


                builder.append("SELECT ");

                boolean hasSelect = false;

                for(ResourceClass resourceClass : expression.getResourceClasses())
                {
                    if(resourceClass instanceof DateTimeConstantZoneClass && splitDateTimeClasses)
                    {
                        appendComma(builder, hasSelect);
                        hasSelect = true;

                        builder.append("CASE ");
                        builder.append(xsdDateTime.getPatternCode(columnName, 1, expression.isBoxed()));
                        builder.append(" WHEN '");
                        builder.append(((DateTimeConstantZoneClass) resourceClass).getZone());
                        builder.append("'::int4 THEN ");
                        builder.append(resourceClass.getPatternCode(columnName, 0, expression.isBoxed()));
                        builder.append(" END AS ");
                        builder.append(resourceClass.getSqlColumn(variableName, 0));
                    }
                    else if(resourceClass instanceof DateConstantZoneClass && splitDateClasses)
                    {
                        appendComma(builder, hasSelect);
                        hasSelect = true;

                        builder.append("CASE ");
                        builder.append(xsdDate.getPatternCode(columnName, 1, expression.isBoxed()));
                        builder.append(" WHEN '");
                        builder.append(((DateConstantZoneClass) resourceClass).getZone());
                        builder.append("'::int4 THEN ");
                        builder.append(resourceClass.getPatternCode(columnName, 0, expression.isBoxed()));
                        builder.append(" END AS ");
                        builder.append(resourceClass.getSqlColumn(variableName, 0));
                    }
                    else if(resourceClass instanceof LangStringConstantTagClass && splitLangClasses)
                    {
                        appendComma(builder, hasSelect);
                        hasSelect = true;

                        builder.append("CASE ");
                        builder.append(rdfLangString.getPatternCode(columnName, 1, expression.isBoxed()));
                        builder.append(" WHEN '");
                        builder.append(((LangStringConstantTagClass) resourceClass).getTag());
                        builder.append("'::varchar THEN ");
                        builder.append(resourceClass.getPatternCode(columnName, 0, expression.isBoxed()));
                        builder.append(" END AS ");
                        builder.append(resourceClass.getSqlColumn(variableName, 0));
                    }
                    else
                    {
                        for(int i = 0; i < resourceClass.getPatternPartsCount(); i++)
                        {
                            appendComma(builder, hasSelect);
                            hasSelect = true;

                            builder.append(resourceClass.getPatternCode(columnName, i, expression.isBoxed()));
                            builder.append(" AS ");
                            builder.append(resourceClass.getSqlColumn(variableName, i));
                        }
                    }
                }

                for(UsedVariable variable : context.variables.getValues())
                {
                    for(ResourceClass resClass : variable.getClasses())
                    {
                        for(int i = 0; i < resClass.getPatternPartsCount(); i++)
                        {
                            appendComma(builder, true);
                            builder.append(resClass.getSqlColumn(variable.getName(), i));
                        }
                    }
                }

                builder.append(" FROM (");
            }

            builder.append("SELECT ");
            builder.append(expression.translate());
            builder.append(" AS ");
            builder.append(columnName);
        }


        for(UsedVariable variable : context.variables.getValues())
        {
            for(ResourceClass resClass : variable.getClasses())
            {
                for(int i = 0; i < resClass.getPatternPartsCount(); i++)
                {
                    appendComma(builder, true);
                    builder.append(resClass.getSqlColumn(variable.getName(), i));
                }
            }
        }

        builder.append(" FROM (");
        builder.append(context.translate());
        builder.append(" ) AS tab");

        if(useTwoPhases)
            builder.append(" ) AS tab");

        return builder.toString();
    }
}