package cz.iocb.chemweb.server.sparql.engine;

import static cz.iocb.chemweb.server.sparql.parser.BuiltinTypes.xsdBooleanIri;
import static cz.iocb.chemweb.server.sparql.parser.BuiltinTypes.xsdDateIri;
import static cz.iocb.chemweb.server.sparql.parser.BuiltinTypes.xsdDateTimeIri;
import static cz.iocb.chemweb.server.sparql.parser.BuiltinTypes.xsdDayTimeDurationIri;
import static cz.iocb.chemweb.server.sparql.parser.BuiltinTypes.xsdDecimalIri;
import static cz.iocb.chemweb.server.sparql.parser.BuiltinTypes.xsdDoubleIri;
import static cz.iocb.chemweb.server.sparql.parser.BuiltinTypes.xsdFloatIri;
import static cz.iocb.chemweb.server.sparql.parser.BuiltinTypes.xsdIntIri;
import static cz.iocb.chemweb.server.sparql.parser.BuiltinTypes.xsdIntegerIri;
import static cz.iocb.chemweb.server.sparql.parser.BuiltinTypes.xsdLongIri;
import static cz.iocb.chemweb.server.sparql.parser.BuiltinTypes.xsdShortIri;
import static cz.iocb.chemweb.server.sparql.parser.BuiltinTypes.xsdStringIri;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import cz.iocb.chemweb.server.sparql.mapping.classes.ResultTag;



public class Result implements AutoCloseable
{
    private static final DecimalFormat decimalFormat;
    private static final long USECS_PER_DAY = 86400000000l;
    private static final long USECS_PER_HOUR = 3600000000l;
    private static final long USECS_PER_MINUTE = 60000000l;
    private static final long USECS_PER_SEC = 1000000l;
    private static final char[] encodeTable = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D',
            'E', 'F' };

    private final HashMap<String, Integer> varNames = new HashMap<String, Integer>();
    private final HashMap<String, Integer> columnNames = new HashMap<String, Integer>();
    private final Vector<String> heads = new Vector<String>();
    private final ResultSet rs;
    private final ResultSetMetaData metadata;
    private final RdfNode[] rowData;


    static
    {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        symbols.setExponentSeparator("E");
        symbols.setInfinity("INF");
        symbols.setNaN("NaN");
        decimalFormat = new DecimalFormat("################0.0################", symbols);
    }


    public Result(ResultSet rs) throws SQLException
    {
        this.rs = rs;
        this.metadata = rs.getMetaData();

        String lastName = null;

        for(int i = 0; i < metadata.getColumnCount(); i++)
        {
            String column = metadata.getColumnName(i + 1);
            String name = column.replaceAll("#.*", "");

            if(!name.equals(lastName))
            {
                lastName = name;
                varNames.put(name, heads.size());
                heads.add(name);
            }

            columnNames.put(column, heads.size() - 1);
        }

        this.rowData = new RdfNode[heads.size()];
    }


    public boolean next() throws SQLException
    {
        if(!rs.next())
            return false;

        for(int i = 0; i < rowData.length; i++)
            rowData[i] = null;

        for(int i = 0; i < metadata.getColumnCount(); i++)
        {
            String name = metadata.getColumnName(i + 1);
            String tagName = name.replaceAll(".*#", "");
            String lexical = rs.getString(i + 1);
            Object value = rs.getObject(i + 1);
            int idx = columnNames.get(name);

            ResultTag tag = ResultTag.get(tagName);

            if(lexical != null)
            {
                switch(tag)
                {
                    case NULL:
                        rowData[idx] = null;
                        break;

                    case BLANKNODEINT:
                        rowData[idx] = new BlankNode("I" + value);
                        break;

                    case BLANKNODESTR:
                        rowData[idx] = new BlankNode(encodeBlankNodeLabel((String) value));
                        break;

                    case IRI:
                        rowData[idx] = new IriNode((String) value);
                        break;

                    case BOOLEAN:
                        rowData[idx] = new TypedLiteral(value.toString(), xsdBooleanIri);
                        break;

                    case SHORT:
                        rowData[idx] = new TypedLiteral(value.toString(), xsdShortIri);
                        break;

                    case INT:
                        rowData[idx] = new TypedLiteral(value.toString(), xsdIntIri);
                        break;

                    case LONG:
                        rowData[idx] = new TypedLiteral(value.toString(), xsdLongIri);
                        break;

                    case FLOAT:
                        Object data = Float.isFinite((float) value) ? new BigDecimal(value.toString()) : value;
                        rowData[idx] = new TypedLiteral(decimalFormat.format(data), xsdFloatIri);
                        break;

                    case DOUBLE:
                        rowData[idx] = new TypedLiteral(decimalFormat.format(value), xsdDoubleIri);
                        break;

                    case INTEGER:
                        rowData[idx] = new TypedLiteral(((BigDecimal) value).stripTrailingZeros().toPlainString(),
                                xsdIntegerIri);
                        break;

                    case DECIMAL:
                        rowData[idx] = new TypedLiteral(((BigDecimal) value).stripTrailingZeros().toPlainString(),
                                xsdDecimalIri);
                        break;

                    case DATETIME:
                        rowData[idx] = new TypedLiteral(lexical, xsdDateTimeIri);
                        break;

                    case DATE:
                        rowData[idx] = new TypedLiteral(lexical, xsdDateIri);
                        break;

                    case DAYTIMEDURATION:
                        rowData[idx] = new TypedLiteral(durationToString((Long) value), xsdDayTimeDurationIri);
                        break;

                    case STRING:
                        rowData[idx] = new TypedLiteral(value.toString(), xsdStringIri);
                        break;

                    case LANGSTRING:
                        String lang = rs.getString(name.replaceAll("#.*", "") + "#" + ResultTag.LANG.getTag());
                        rowData[idx] = new LanguageTaggedLiteral(value.toString(), lang);
                        break;

                    case LITERAL:
                        String type = rs.getString(name.replaceAll("#.*", "") + "#" + ResultTag.TYPE.getTag());
                        rowData[idx] = new TypedLiteral(value.toString(), type);

                    case LANG:
                    case TYPE:
                        // ignore supplementary literal tags
                        break;
                }
            }
        }

        return true;
    }


    public List<String> getWarnings() throws SQLException
    {
        LinkedList<String> warnings = new LinkedList<String>();

        for(SQLWarning warning = rs.getStatement().getWarnings(); warning != null; warning = warning.getNextWarning())
            warnings.add(warning.getMessage());

        return warnings;
    }


    public Vector<String> getHeads()
    {
        return heads;
    }


    public HashMap<String, Integer> getVariableIndexes()
    {
        return varNames;
    }


    public RdfNode get(int idx)
    {
        return rowData[idx];
    }


    public RdfNode get(String name)
    {
        Integer idx = varNames.get(name);

        if(idx == null)
            return null;

        return rowData[idx];
    }


    public RdfNode[] getRow()
    {
        return rowData.clone();
    }


    @Override
    public void close() throws SQLException
    {
        rs.close();
    }


    private static String encodeBlankNodeLabel(String value)
    {
        byte[] data = value.getBytes(StandardCharsets.UTF_8);

        StringBuffer buffer = new StringBuffer();
        buffer.append('S');

        for(int j = 0; j < data.length; j++)
        {
            if((data[j] < '0' || data[j] > '9') && (data[j] < 'A' || data[j] > 'Z') && (data[j] < 'a' || data[j] > 'z'))
            {
                int val = data[j] < 0 ? data[j] + 256 : data[j];
                buffer.append('_');
                buffer.append(encodeTable[val / 16]);
                buffer.append(encodeTable[val % 16]);
            }
            else
            {
                buffer.append((char) data[j]);
            }
        }

        return buffer.toString();
    }


    private static String durationToString(long value)
    {
        if(value == 0)
            return "PT0S";

        if(value == Long.MAX_VALUE)
            return "-P106751991DT4H54.775808S";

        StringBuffer buffer = new StringBuffer();

        if(value < 0)
            buffer.append('-');

        value = Math.abs(value);

        long days = value / USECS_PER_DAY;
        long hours = value % USECS_PER_DAY / USECS_PER_HOUR;
        long minutes = value % USECS_PER_HOUR / USECS_PER_MINUTE;
        long seconds = value % USECS_PER_MINUTE / USECS_PER_SEC;
        long useconds = value % USECS_PER_SEC;

        buffer.append('P');

        if(days > 0)
        {
            buffer.append(days);
            buffer.append('D');
        }

        if(hours > 0 || minutes > 0 || seconds > 0 || useconds > 0)
            buffer.append('T');

        if(hours > 0)
        {
            buffer.append(hours);
            buffer.append('H');
        }

        if(minutes > 0)
        {
            buffer.append(minutes);
            buffer.append('M');
        }

        if(seconds > 0)
        {
            buffer.append(seconds);

            if(useconds == 0)
                buffer.append('S');
        }

        if(useconds > 0)
        {
            if(seconds == 0)
                buffer.append('0');

            String str = Long.toString(useconds);

            buffer.append('.');
            buffer.append("000000".substring(str.length(), 6) + str.replaceFirst("0+$", ""));
            buffer.append('S');
        }

        return buffer.toString();
    }
}