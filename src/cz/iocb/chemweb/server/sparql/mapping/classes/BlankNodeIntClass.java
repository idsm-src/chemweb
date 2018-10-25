package cz.iocb.chemweb.server.sparql.mapping.classes;

import static cz.iocb.chemweb.server.sparql.mapping.classes.ResultTag.BLANKNODEINT;
import cz.iocb.chemweb.server.sparql.translator.expression.VariableAccessor;



public class BlankNodeIntClass extends BlankNodeClass
{
    BlankNodeIntClass()
    {
        super("int_blanknode", "int8", BLANKNODEINT);
    }


    @Override
    public String getPatternCode(String column, int part, boolean isBoxed)
    {
        if(isBoxed == false)
            throw new IllegalArgumentException();

        return "sparql.rdfbox_extract_int_blanknode" + "(" + column + ")";
    }


    @Override
    public String getExpressionCode(String variable, VariableAccessor variableAccessor, boolean rdfbox)
    {
        return "sparql.cast_as_rdfbox_from_int_blanknode(" + variableAccessor.getSqlVariableAccess(variable, this, 0)
                + ")";
    }
}