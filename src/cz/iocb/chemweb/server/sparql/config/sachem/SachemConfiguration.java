package cz.iocb.chemweb.server.sparql.config.sachem;

import static java.util.Arrays.asList;
import java.sql.SQLException;
import javax.sql.DataSource;
import cz.iocb.chemweb.server.sparql.config.SparqlDatabaseConfiguration;
import cz.iocb.chemweb.server.sparql.config.common.Common;
import cz.iocb.chemweb.server.sparql.database.DatabaseSchema;
import cz.iocb.chemweb.server.sparql.database.Table;
import cz.iocb.chemweb.server.sparql.database.TableColumn;
import cz.iocb.chemweb.server.sparql.mapping.classes.IntegerUserIriClass;



public class SachemConfiguration extends SparqlDatabaseConfiguration
{
    public SachemConfiguration(String service, DataSource connectionPool, DatabaseSchema schema, String index,
            String iriPrefix, int idLength) throws SQLException
    {
        super(service, connectionPool, schema);

        addPrefixes();
        addResourceClasses(index, iriPrefix, idLength);
        addQuadMappings(index);
        addProcedures(index);
    }


    private void addPrefixes()
    {
        Common.addPrefixes(this);
        Sachem.addPrefixes(this);
        MolFiles.addPrefixes(this);
    }


    private void addResourceClasses(String index, String iriPrefix, int idLength)
    {
        Sachem.addResourceClasses(this);

        addIriClass(new IntegerUserIriClass(index + ":compound", "integer", iriPrefix, idLength));
        addIriClass(new IntegerUserIriClass(index + ":molfile", "integer", iriPrefix, idLength, "_Molfile"));
    }


    private void addQuadMappings(String index)
    {
        MolFiles.addQuadMappings(this, index + ":compound", index + ":molfile", new Table("molecules", index),
                asList(new TableColumn("id")));
    }


    private void addProcedures(String index)
    {
        Sachem.addProcedures(this, index, index + ":compound", asList(new TableColumn("compound")));
    }
}
