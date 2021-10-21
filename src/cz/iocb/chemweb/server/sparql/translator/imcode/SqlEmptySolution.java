package cz.iocb.chemweb.server.sparql.translator.imcode;

import java.util.Set;
import cz.iocb.chemweb.server.sparql.translator.UsedVariables;



public class SqlEmptySolution extends SqlIntercode
{
    static private final SqlEmptySolution singleton = new SqlEmptySolution();


    private SqlEmptySolution()
    {
        super(new UsedVariables(), true);
    }


    public static SqlEmptySolution get()
    {
        return singleton;
    }


    @Override
    public SqlIntercode optimize(Set<String> restrictions, boolean reduced)
    {
        return this;
    }


    @Override
    public String translate()
    {
        return "SELECT 1";
    }
}
