package cz.iocb.chemweb.server.sparql.mapping.classes;

import static cz.iocb.chemweb.server.sparql.mapping.classes.BuiltinClasses.xsdDateTime;
import static cz.iocb.chemweb.server.sparql.mapping.classes.BuiltinDataTypes.xsdDateTimeIri;
import static cz.iocb.chemweb.server.sparql.mapping.classes.ResultTag.DATETIME;
import static java.util.Arrays.asList;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import cz.iocb.chemweb.server.sparql.database.Column;
import cz.iocb.chemweb.server.sparql.database.ConstantColumn;
import cz.iocb.chemweb.server.sparql.database.ExpressionColumn;
import cz.iocb.chemweb.server.sparql.parser.model.expression.Literal;
import cz.iocb.chemweb.server.sparql.parser.model.triple.Node;



public class DateTimeClass extends LiteralClass
{
    @SuppressWarnings("serial") private static final Map<Long, String> era = new HashMap<Long, String>()
    {
        {
            put(0l, " BC");
            put(1l, "");
        }
    };

    private static final DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder()//
            .appendValue(ChronoField.YEAR_OF_ERA, 4, 19, SignStyle.NORMAL).appendLiteral("-")//
            .appendValue(ChronoField.MONTH_OF_YEAR, 2).appendLiteral("-")//
            .appendValue(ChronoField.DAY_OF_MONTH, 2).appendLiteral("T")//
            .appendValue(ChronoField.HOUR_OF_DAY, 2).appendLiteral(":")//
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2).appendLiteral(":")//
            .appendValue(ChronoField.SECOND_OF_MINUTE, 2)//
            .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)//
            .appendOffset("+HH:MM", "Z")//
            .appendText(ChronoField.ERA, era)//
            .toFormatter(Locale.ENGLISH);

    private static final DateTimeFormatter localDateTimeFormatter = new DateTimeFormatterBuilder()//
            .appendValue(ChronoField.YEAR_OF_ERA, 4, 19, SignStyle.NORMAL).appendLiteral("-")//
            .appendValue(ChronoField.MONTH_OF_YEAR, 2).appendLiteral("-")//
            .appendValue(ChronoField.DAY_OF_MONTH, 2).appendLiteral("T")//
            .appendValue(ChronoField.HOUR_OF_DAY, 2).appendLiteral(":")//
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2).appendLiteral(":")//
            .appendValue(ChronoField.SECOND_OF_MINUTE, 2)//
            .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)//
            .appendLiteral("Z")//
            .appendText(ChronoField.ERA, era)//
            .toFormatter(Locale.ENGLISH);


    DateTimeClass()
    {
        super("datetime", asList("timestamptz", "int4"), asList(DATETIME), xsdDateTimeIri);
    }


    @Override
    public ResourceClass getGeneralClass()
    {
        return xsdDateTime;
    }


    @Override
    public List<Column> toColumns(Node node)
    {
        List<Column> result = new ArrayList<Column>(getColumnCount());

        result.add(new ConstantColumn("'" + getDateTime((Literal) node) + "'::timestamptz"));
        result.add(new ConstantColumn("'" + getZone((Literal) node) + "'::int4"));

        return result;
    }


    @Override
    public List<Column> fromGeneralClass(List<Column> columns)
    {
        return columns;
    }


    @Override
    public List<Column> toGeneralClass(List<Column> columns, boolean check)
    {
        return columns;
    }


    @Override
    public List<Column> fromExpression(Column column, boolean isBoxed, boolean check)
    {
        String prefix = isBoxed ? "sparql.rdfbox_extract_datetime" : "sparql.zoneddatetime";
        List<Column> result = new ArrayList<Column>(getColumnCount());

        result.add(new ExpressionColumn(prefix + "_date(" + column + ")"));
        result.add(new ExpressionColumn(prefix + "_zone(" + column + ")"));

        return result;
    }


    @Override
    public Column toExpression(Node node)
    {
        return new ConstantColumn("'" + ((Literal) node).getValue() + "'::sparql.zoneddatetime");
    }


    @Override
    public Column toExpression(List<Column> columns, boolean rdfbox)
    {
        String function = rdfbox ? "sparql.cast_as_rdfbox_from_datetime" : "sparql.zoneddatetime_create";

        return new ExpressionColumn(function + "(" + columns.get(0) + ", " + columns.get(1) + ")");
    }


    @Override
    public List<Column> toResult(List<Column> columns)
    {
        // xsdDateTime is returned as zoneddatetime because there are many discrepancies in timestamp interpretations
        return asList(
                new ExpressionColumn("sparql.zoneddatetime_create(" + columns.get(0) + ", " + columns.get(1) + ")"));
    }


    public static String getDateTime(Literal literal)
    {
        if(literal.getValue() instanceof OffsetDateTime)
            return ((OffsetDateTime) literal.getValue()).atZoneSameInstant(ZoneOffset.UTC).format(dateTimeFormatter);
        else
            return ((LocalDateTime) literal.getValue()).format(localDateTimeFormatter);
    }


    public static int getZone(Literal literal)
    {
        if(literal.getValue() instanceof OffsetDateTime)
            return ((OffsetDateTime) literal.getValue()).getOffset().getTotalSeconds();

        return Integer.MIN_VALUE;
    }
}
