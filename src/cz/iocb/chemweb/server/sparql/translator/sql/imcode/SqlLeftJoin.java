package cz.iocb.chemweb.server.sparql.translator.sql.imcode;

import java.util.ArrayList;
import cz.iocb.chemweb.server.sparql.mapping.classes.ResourceClass;
import cz.iocb.chemweb.server.sparql.translator.sql.UsedPairedVariable;
import cz.iocb.chemweb.server.sparql.translator.sql.UsedPairedVariable.PairedClass;
import cz.iocb.chemweb.server.sparql.translator.sql.UsedVariable;
import cz.iocb.chemweb.server.sparql.translator.sql.UsedVariables;



public class SqlLeftJoin extends SqlIntercode
{
    private static final String leftTable = "tab0";
    private static final String rightTable = "tab1";

    private final SqlIntercode left;
    private final SqlIntercode right;


    protected SqlLeftJoin(UsedVariables variables, SqlIntercode left, SqlIntercode right)
    {
        super(variables);
        this.left = left;
        this.right = right;
    }


    public static SqlIntercode leftJoin(SqlIntercode left, SqlIntercode right)
    {
        /* special cases */

        if(left instanceof SqlNoSolution)
            return new SqlNoSolution();

        if(right instanceof SqlNoSolution)
            return left;

        if(left instanceof SqlEmptySolution)
            return right;

        if(right instanceof SqlEmptySolution)
            return left;

        if(right instanceof SqlUnion)
            right = optimizeRightUnion(left.getVariables(), (SqlUnion) right);


        /* standard left join */

        ArrayList<UsedPairedVariable> pairs = UsedPairedVariable.getPairs(left.getVariables(), right.getVariables());
        UsedVariables variables = new UsedVariables();


        for(UsedPairedVariable pair : pairs)
        {
            UsedVariable leftVariable = pair.getLeftVariable();


            String var = pair.getName();
            boolean canBeLeftNull = leftVariable == null ? true : leftVariable.canBeNull();

            UsedVariable variable = new UsedVariable(var, canBeLeftNull);
            variables.add(variable);


            for(PairedClass pairedClass : pair.getClasses())
            {
                if(pairedClass.getLeftClass() == null)
                {
                    if(leftVariable != null && !leftVariable.canBeNull())
                        continue;

                    variable.addClass(pairedClass.getRightClass());
                }
                else if(pairedClass.getRightClass() == null)
                {
                    variable.addClass(pairedClass.getLeftClass());
                }
                else
                {
                    assert pairedClass.getLeftClass() == pairedClass.getRightClass();
                    variable.addClass(pairedClass.getLeftClass());
                }
            }
        }


        return new SqlLeftJoin(variables, left, right);
    }


    private static SqlIntercode optimizeRightUnion(UsedVariables variables, SqlUnion union)
    {
        SqlIntercode newUnion = null;

        for(SqlIntercode child : union.getChilds())
        {
            if(areVariablesCompatible(variables, child.getVariables()))
            {
                if(newUnion == null)
                    newUnion = child;
                else
                    newUnion = SqlUnion.union(newUnion, child);
            }
        }

        return newUnion;
    }


    private static boolean areVariablesCompatible(UsedVariables left, UsedVariables right)
    {
        for(UsedVariable leftVariable : left.getValues())
        {
            if(leftVariable.canBeNull())
                continue;

            UsedVariable rightVariable = right.get(leftVariable.getName());


            if(rightVariable == null || rightVariable.canBeNull())
                continue;

            boolean isCorrect = false;

            for(ResourceClass resClass : leftVariable.getClasses())
                if(rightVariable.getClasses().contains(resClass))
                    isCorrect = true;

            if(!isCorrect)
                return false;
        }

        return true;
    }


    @Override
    public String translate()
    {
        /* join */

        ArrayList<UsedPairedVariable> pairs = UsedPairedVariable.getPairs(left.getVariables(), right.getVariables());
        UsedVariables variables = new UsedVariables();
        StringBuilder builder = new StringBuilder();


        builder.append("SELECT ");

        boolean hasSelect = false;

        for(UsedPairedVariable pair : pairs)
        {
            UsedVariable leftVariable = pair.getLeftVariable();
            UsedVariable rightVariable = pair.getRightVariable();


            String var = pair.getName();
            boolean canBeLeftNull = leftVariable == null ? true : leftVariable.canBeNull();

            UsedVariable variable = new UsedVariable(var, canBeLeftNull);
            variables.add(variable);


            for(PairedClass pairedClass : pair.getClasses())
            {
                if(pairedClass.getLeftClass() == null)
                {
                    if(leftVariable != null && !leftVariable.canBeNull())
                        continue;

                    variable.addClass(pairedClass.getRightClass());
                }
                else if(pairedClass.getRightClass() == null)
                {
                    variable.addClass(pairedClass.getLeftClass());
                }
                else
                {
                    assert pairedClass.getLeftClass() == pairedClass.getRightClass();
                    variable.addClass(pairedClass.getLeftClass());
                }


                ResourceClass resClass = pairedClass.getLeftClass() != null ? pairedClass.getLeftClass()
                        : pairedClass.getRightClass();

                for(int i = 0; i < resClass.getPartsCount(); i++)
                {
                    appendComma(builder, hasSelect);
                    hasSelect = true;

                    generateJoinSelectVarable(builder, leftVariable, rightVariable, resClass.getSqlColumn(var, i));
                }
            }
        }


        builder.append(" FROM ");
        builder.append("(");
        builder.append(left.translate());
        builder.append(") AS ");
        builder.append(leftTable);

        builder.append(" LEFT OUTER JOIN (");
        builder.append(right.translate());
        builder.append(") AS ");
        builder.append(rightTable);


        builder.append(" ON ");

        boolean hasWhere = false;

        for(UsedPairedVariable pair : pairs)
        {
            String var = pair.getName();
            UsedVariable leftVariable = pair.getLeftVariable();
            UsedVariable rightVariable = pair.getRightVariable();

            if(leftVariable != null && rightVariable != null)
            {
                appendAnd(builder, hasWhere);
                hasWhere = true;

                builder.append("(");
                boolean restricted = false;

                if(leftVariable.canBeNull())
                {
                    boolean use = false;
                    builder.append("(");

                    for(ResourceClass resClass : leftVariable.getClasses())
                    {
                        for(int i = 0; i < resClass.getPartsCount(); i++)
                        {
                            appendAnd(builder, use);
                            use = true;

                            builder.append(leftTable).append('.').append(resClass.getSqlColumn(var, i));
                            builder.append(" IS NULL");
                        }
                    }

                    builder.append(")");

                    assert use;
                    restricted = true;
                }

                if(rightVariable.canBeNull())
                {
                    appendOr(builder, restricted);
                    restricted = true;

                    boolean use = false;
                    builder.append("(");

                    for(ResourceClass resClass : rightVariable.getClasses())
                    {
                        for(int i = 0; i < resClass.getPartsCount(); i++)
                        {
                            appendAnd(builder, use);
                            use = true;

                            builder.append(rightTable).append('.').append(resClass.getSqlColumn(var, i));
                            builder.append(" IS NULL");
                        }
                    }

                    builder.append(")");

                    assert use;
                }

                for(PairedClass pairedClass : pair.getClasses())
                {
                    if(pairedClass.getLeftClass() != null && pairedClass.getRightClass() != null)
                    {
                        assert pairedClass.getLeftClass() == pairedClass.getRightClass();
                        ResourceClass resClass = pairedClass.getLeftClass();

                        appendOr(builder, restricted);
                        restricted = true;

                        builder.append("(");

                        for(int i = 0; i < resClass.getPartsCount(); i++)
                        {
                            appendAnd(builder, i > 0);

                            builder.append(leftTable).append('.').append(resClass.getSqlColumn(var, i));
                            builder.append(" = ");
                            builder.append(rightTable).append('.').append(resClass.getSqlColumn(var, i));
                        }

                        builder.append(")");
                    }
                }

                builder.append(")");

                assert restricted;
            }
        }

        if(!hasWhere)
            builder.append("true");


        return builder.toString();
    }


    private void generateJoinSelectVarable(StringBuilder builder, UsedVariable leftVariable, UsedVariable rightVariable,
            String var)
    {
        if(leftVariable == null)
        {
            builder.append(rightTable).append('.').append(var);
        }
        else if(rightVariable == null)
        {
            builder.append(leftTable).append('.').append(var);
        }
        else if(!leftVariable.canBeNull())
        {
            builder.append(leftTable).append('.').append(var);
        }
        else if(!rightVariable.canBeNull())
        {
            builder.append(rightTable).append('.').append(var);
        }
        else
        {
            builder.append("COALESCE(");
            builder.append(leftTable).append('.').append(var);
            builder.append(", ");
            builder.append(rightTable).append('.').append(var);
            builder.append(")");
        }

        builder.append(" AS ");
        builder.append(var);
    }
}