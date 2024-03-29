package cz.iocb.chemweb.server.sparql.config.ontology;

import static java.util.Arrays.asList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import cz.iocb.chemweb.server.sparql.config.SparqlDatabaseConfiguration;
import cz.iocb.chemweb.server.sparql.database.Column;
import cz.iocb.chemweb.server.sparql.database.ConstantColumn;
import cz.iocb.chemweb.server.sparql.database.SQLRuntimeException;
import cz.iocb.chemweb.server.sparql.engine.IriCache;
import cz.iocb.chemweb.server.sparql.engine.Request;
import cz.iocb.chemweb.server.sparql.mapping.classes.GeneralUserIriClass;
import cz.iocb.chemweb.server.sparql.parser.model.IRI;
import cz.iocb.chemweb.server.sparql.parser.model.triple.Node;



public class OntologyResource extends GeneralUserIriClass
{
    private static class Unit
    {
        short id;
        int valueOffset;
        String pattern;

        Unit(short id, int valueOffset, String pattern)
        {
            this.id = id;
            this.valueOffset = valueOffset;
            this.pattern = pattern;
        }
    }


    private static final IriCache cache = new IriCache(1000, 10);
    private static final String sqlQuery = "select resource_id from ontology.resources__reftable where iri = ?";
    private static OntologyResource instance;
    private static List<Unit> units;

    private static final int unitUncategorized = 0;
    private static final int unitPR0 = 31;
    private static final int unitPR1 = 32;
    private static final int unitPR2 = 33;
    private static final int unitAT = 34;
    private static final int unitZDBGENE = 35;
    private static final int unitStar = 95;
    private static final int unitRareDiseases = 180;


    private OntologyResource()
    {
        super("ontology:resource", "ontology", "ontology_resource", asList("smallint", "integer"),
                units.stream().map(c -> c.pattern).collect(Collectors.joining("|")),
                GeneralUserIriClass.SqlCheck.IF_NOT_MATCH);
    }


    @Override
    public List<Column> toColumns(Node node)
    {
        IRI iri = (IRI) node;
        String val = (iri).getValue();

        assert match(iri);

        for(Unit unit : units)
        {
            if(val.matches(unit.pattern))
            {
                String tail = val.substring(unit.valueOffset);
                int id = 0;

                if(unit.id == unitPR0)
                {
                    // [0-9][A-Z0-9][0-9][A-Z0-9]{3}[0-9]
                    id = tail.charAt(0) - '0';
                    id = id * 36 + code(tail.charAt(1));
                    id = id * 10 + tail.charAt(2) - '0';
                    id = id * 36 + code(tail.charAt(3));
                    id = id * 36 + code(tail.charAt(4));
                    id = id * 36 + code(tail.charAt(5));
                    id = id * 10 + tail.charAt(6) - '0';
                }
                else if(unit.id == unitPR1 || unit.id == unitPR2)
                {
                    // [A-Z][0-9][A-Z0-9]{3}[0-9](-([12])?[0-9])?
                    id = tail.charAt(0) - 'A';
                    id = id * 10 + tail.charAt(1) - '0';
                    id = id * 36 + code(tail.charAt(2));
                    id = id * 36 + code(tail.charAt(3));
                    id = id * 36 + code(tail.charAt(4));
                    id = id * 10 + tail.charAt(5) - '0';

                    if(unit.id == unitPR1)
                        id = id * 30 + Integer.parseInt(tail.substring(7));
                }
                else if(unit.id == unitAT)
                {
                    // [A-Z0-9]G[0-9]{5}
                    id = code(tail.charAt(0)) * 100000 + Integer.parseInt(tail.substring(2));
                }
                else if(unit.id == unitZDBGENE)
                {
                    // [0-9]{6}-([1-3])?[0-9]{1,3}$
                    id = Integer.parseInt(tail.substring(0, 6));
                    id = id * 4000 + Integer.parseInt(tail.substring(7));
                }
                else if(unit.id == unitStar)
                {
                    id = tail.charAt(0) - '0';
                }
                else if(unit.id == unitRareDiseases)
                {
                    id = Integer.parseInt(tail.substring(0, tail.length() - 6));
                }
                else
                {
                    id = Integer.parseInt(tail);
                }

                List<Column> columns = new ArrayList<Column>();
                columns.add(new ConstantColumn("'" + unit.id + "'::" + sqlTypes.get(0)));
                columns.add(new ConstantColumn("'" + id + "'::" + sqlTypes.get(1)));

                return columns;
            }
        }


        List<Column> hit = cache.getFromCache(iri, this);

        if(hit != null)
            return hit;


        try(PreparedStatement statement = Request.currentRequest().getStatement(sqlQuery))
        {
            statement.setString(1, val);

            try(ResultSet result = statement.executeQuery())
            {
                result.next();

                List<Column> columns = new ArrayList<Column>();
                columns.add(new ConstantColumn("'" + unitUncategorized + "'::" + sqlTypes.get(0)));
                columns.add(new ConstantColumn("'" + result.getInt(1) + "'::" + sqlTypes.get(1)));

                cache.storeToCache(iri, this, columns);
                return columns;
            }
        }
        catch(SQLException e)
        {
            throw new SQLRuntimeException(e);
        }
    }


    @Override
    public boolean match(IRI iri)
    {
        String val = (iri).getValue();

        for(Unit unit : units)
            if(val.matches(unit.pattern))
                return true;


        List<Column> hit = cache.getFromCache(iri, this);

        if(hit == IriCache.mismatch)
            return false;
        else if(hit != null)
            return true;


        try(PreparedStatement statement = Request.currentRequest().getStatement(sqlQuery))
        {
            statement.setString(1, val);

            try(ResultSet result = statement.executeQuery())
            {
                if(!result.next())
                {
                    cache.storeToCache(iri, this, IriCache.mismatch);
                    return false;
                }

                List<Column> columns = new ArrayList<Column>();
                columns.add(new ConstantColumn("'" + unitUncategorized + "'::" + sqlTypes.get(0)));
                columns.add(new ConstantColumn("'" + result.getInt(1) + "'::" + sqlTypes.get(1)));

                cache.storeToCache(iri, this, columns);

                return true;
            }
        }
        catch(SQLException e)
        {
            throw new SQLRuntimeException(e);
        }
    }


    private static int code(char value)
    {
        return value > '9' ? 10 + value - 'A' : value - '0';
    }


    public static synchronized OntologyResource get(SparqlDatabaseConfiguration config) throws SQLException
    {
        if(instance != null)
            return instance;

        units = new ArrayList<Unit>();

        try(Connection connection = config.getConnectionPool().getConnection())
        {
            try(Statement statement = connection.createStatement())
            {
                try(ResultSet result = statement.executeQuery("select unit_id, value_offset - 1, pattern "
                        + "from ontology.resource_categories__reftable order by unit_id"))
                {
                    while(result.next())
                        units.add(new Unit(result.getShort(1), result.getInt(2), result.getString(3)));
                }
            }
        }

        instance = new OntologyResource();

        return instance;
    }
}
