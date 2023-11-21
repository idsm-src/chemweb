package cz.iocb.chemweb.server.sparql.mapping.classes;

import static java.util.Arrays.asList;
import java.util.ArrayList;
import java.util.List;
import cz.iocb.chemweb.server.sparql.database.Column;
import cz.iocb.chemweb.server.sparql.database.ConstantColumn;
import cz.iocb.chemweb.server.sparql.database.ExpressionColumn;
import cz.iocb.chemweb.server.sparql.mapping.BlankNodeLiteral;
import cz.iocb.chemweb.server.sparql.parser.model.triple.Node;



public class UserIntBlankNodeClass extends IntBlankNodeClass
{
    static private int counter;

    private final int segment;


    public UserIntBlankNodeClass()
    {
        super("iblanknode-" + Integer.toHexString(counter), asList("int4"));
        this.segment = counter++;
    }


    public UserIntBlankNodeClass(int segment)
    {
        super("iblanknode-" + Integer.toHexString(segment), asList("int4"));
        this.segment = segment;

        if(segment >= 0)
            throw new IllegalArgumentException();
    }


    @Override
    public List<Column> toColumns(Node node)
    {
        return asList(new ConstantColumn("'" + ((BlankNodeLiteral) node).getLabel() + "'::int4"));
    }


    @Override
    public List<Column> fromGeneralClass(List<Column> columns)
    {
        StringBuilder builder = new StringBuilder();

        builder.append("CASE WHEN ");
        builder.append(columns.get(1));
        builder.append(" = '");
        builder.append(segment);
        builder.append("'::int4 THEN ");
        builder.append(columns.get(0));
        builder.append(" END");

        return asList(new ExpressionColumn(builder.toString()));
    }


    @Override
    public List<Column> toGeneralClass(List<Column> columns, boolean check)
    {
        List<Column> result = new ArrayList<Column>(getGeneralClass().getColumnCount());

        result.add(columns.get(0));

        if(check == false)
        {
            result.add(new ConstantColumn("'" + segment + "'::int4"));
        }
        else
        {
            StringBuilder builder = new StringBuilder();

            builder.append("CASE WHEN ");
            builder.append(columns.get(0));
            builder.append(" IS NOT NULL THEN '");
            builder.append(segment);
            builder.append("'::int4 END");

            result.add(new ExpressionColumn(builder.toString()));
        }

        return result;
    }


    @Override
    public List<Column> fromExpression(Column column)
    {
        return asList(column);
    }


    @Override
    public Column toExpression(List<Column> columns)
    {
        return columns.get(0);
    }


    @Override
    public List<Column> fromBoxedExpression(Column column, boolean check)
    {
        if(check)
            return asList(new ExpressionColumn(
                    "sparql.rdfbox_get_iblanknode_value_of_segment(" + column + ", '" + segment + "'::int4"));
        else
            return asList(new ExpressionColumn("sparql.rdfbox_get_iblanknode_value(" + column + ")"));
    }


    @Override
    public Column toBoxedExpression(List<Column> columns)
    {
        return new ExpressionColumn(
                "sparql.rdfbox_create_from_iblanknode(" + columns.get(0) + ", '" + segment + "'::int4)");
    }


    @Override
    public List<Column> toResult(List<Column> columns)
    {
        return asList(
                new ExpressionColumn("sparql.iblanknode_create(" + columns.get(0) + ", '" + segment + "'::int4)"));
    }


    @Override
    public String fromGeneralExpression(String code)
    {
        return "sparql.iblanknode_get_value_of_segment(" + code + ", '" + segment + "'::int4)";
    }


    @Override
    public String toGeneralExpression(String code)
    {
        return "sparql.iblanknode_create(" + code + ", '" + segment + "'::int4)";
    }


    @Override
    public String toBoxedExpression(String code)
    {
        return "sparql.rdfbox_create_from_iblanknode(" + code + ", '" + segment + "'::int4)";
    }


    @Override
    public String toUnboxedExpression(String code, boolean check)
    {
        if(check)
            return "sparql.rdfbox_get_iblanknode_value_of_segment(" + code + ", '" + segment + "'::int4)";
        else
            return "sparql.rdfbox_get_iblanknode_value(" + code + ")";
    }


    public int getSegment()
    {
        return segment;
    }


    @Override
    public boolean equals(Object object)
    {
        if(object == this)
            return true;

        if(!super.equals(object))
            return false;

        UserIntBlankNodeClass other = (UserIntBlankNodeClass) object;

        if(segment != other.segment)
            return false;

        return true;
    }
}
