package cz.iocb.chemweb.server.sparql.translator.imcode.expression;

import java.util.HashSet;
import cz.iocb.chemweb.server.sparql.mapping.classes.ResourceClass;
import cz.iocb.chemweb.server.sparql.translator.UsedVariables;



public class SqlNull extends SqlExpressionIntercode
{
    static private final SqlNull singleton = new SqlNull();


    private SqlNull()
    {
        super(new HashSet<ResourceClass>(), true, true);
    }


    public static SqlExpressionIntercode get()
    {
        return singleton;
    }


    @Override
    public SqlExpressionIntercode optimize(UsedVariables variables)
    {
        return singleton;
    }


    @Override
    public String translate()
    {
        return "NULL";
    }
}
