package cz.iocb.chemweb.server.sparql.config.sachem;

import static java.util.Arrays.asList;
import java.sql.SQLException;
import javax.sql.DataSource;
import cz.iocb.chemweb.server.sparql.config.SparqlDatabaseConfiguration;
import cz.iocb.chemweb.server.sparql.config.common.Common;
import cz.iocb.chemweb.server.sparql.config.mona.Mona;
import cz.iocb.chemweb.server.sparql.database.DatabaseSchema;
import cz.iocb.chemweb.server.sparql.database.Table;
import cz.iocb.chemweb.server.sparql.database.TableColumn;
import cz.iocb.chemweb.server.sparql.mapping.classes.IntegerUserIriClass;
import cz.iocb.chemweb.server.sparql.mapping.classes.MapUserIriClass;



public class MonaSachemConfiguration extends SparqlDatabaseConfiguration
{
    public MonaSachemConfiguration(String service, DataSource connectionPool, DatabaseSchema schema) throws SQLException
    {
        super(service, connectionPool, schema);

        addPrefixes();
        addResourceClasses();
        addQuadMappings();
        addProcedures();
    }


    private void addPrefixes()
    {
        Common.addPrefixes(this);
        Sachem.addPrefixes(this);
        MolFiles.addPrefixes(this);

        addPrefix("mona", Mona.mona);
        addPrefix("bnmona", Mona.bnmona);
    }


    private void addResourceClasses()
    {
        Sachem.addResourceClasses(this);

        addIriClass(new IntegerUserIriClass("mona:molfile", "integer", Mona.bnmona + "id", "_molfile"));

        addIriClass(new MapUserIriClass("mona:compound", "integer", new Table("mona", "compound_bases"),
                new TableColumn("id"), new TableColumn("accession"), Mona.mona, ".*", "_CMPD"));
    }


    private void addQuadMappings()
    {
        MolFiles.addQuadMappings(this, "mona:compound", "mona:molfile", new Table("mona", "compound_structures"),
                asList(new TableColumn("compound")), "compound", "structure");
    }


    private void addProcedures()
    {
        Sachem.addProcedures(this, "mona", "mona:compound", asList(new TableColumn("compound")));
    }
}
