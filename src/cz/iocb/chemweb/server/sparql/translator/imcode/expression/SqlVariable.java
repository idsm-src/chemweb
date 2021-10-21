package cz.iocb.chemweb.server.sparql.translator.imcode.expression;

import java.util.List;
import java.util.Set;
import cz.iocb.chemweb.server.sparql.database.Column;
import cz.iocb.chemweb.server.sparql.mapping.classes.ResourceClass;
import cz.iocb.chemweb.server.sparql.translator.UsedVariable;
import cz.iocb.chemweb.server.sparql.translator.UsedVariables;



public class SqlVariable extends SqlNodeValue
{
    private final UsedVariable variable;


    protected SqlVariable(UsedVariable variable, Set<ResourceClass> resourceClasses, boolean canBeNull)
    {
        super(resourceClasses, canBeNull);
        this.variable = variable;

        this.referencedVariables.add(variable.getName());
    }


    public static SqlExpressionIntercode create(String variable, UsedVariables variables)
    {
        UsedVariable usedVariable = variables.get(variable);

        if(usedVariable == null)
            return SqlNull.get();

        return new SqlVariable(usedVariable, usedVariable.getClasses(), usedVariable.canBeNull());
    }


    @Override
    public SqlExpressionIntercode optimize(UsedVariables variables)
    {
        return create(variable.getName(), variables);
    }


    @Override
    public String translate()
    {
        if(isBoxed())
            return translateAsBoxedOperand(this, getResourceClasses());
        else
            return translateAsUnboxedOperand(this, getExpressionResourceClass());
    }


    Column getExpressionValue(ResourceClass resourceClass, boolean isBoxed)
    {
        return resourceClass.toExpression(variable.getMapping(resourceClass), isBoxed);
    }


    @Override
    public List<Column> asResource(ResourceClass resourceClass)
    {
        return variable.toResource(resourceClass);
    }


    public String getName()
    {
        return variable.getName();
    }


    public UsedVariable getUsedVariable()
    {
        return variable;
    }
}
