package cz.iocb.chemweb.server.sparql.config.pubchem;

import static cz.iocb.chemweb.server.sparql.config.pubchem.PubChemConfiguration.schema;
import static cz.iocb.chemweb.server.sparql.mapping.classes.BuiltinClasses.xsdString;
import static java.util.Arrays.asList;
import cz.iocb.chemweb.server.sparql.config.SparqlDatabaseConfiguration;
import cz.iocb.chemweb.server.sparql.database.Table;
import cz.iocb.chemweb.server.sparql.mapping.ConstantIriMapping;
import cz.iocb.chemweb.server.sparql.mapping.NodeMapping;
import cz.iocb.chemweb.server.sparql.mapping.classes.GeneralUserIriClass;



public class Measuregroup
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(new GeneralUserIriClass("pubchem:measuregroup", schema, "measuregroup",
                asList("integer", "integer"),
                "http://rdf\\.ncbi\\.nlm\\.nih\\.gov/pubchem/measuregroup/AID[0-9]+(_(PMID([1-9][0-9]*)?|[1-9][0-9]*|0)?)?"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("pubchem:measuregroup");

        {
            Table table = new Table(schema, "measuregroup_bases");
            NodeMapping subject = config.createIriMapping("pubchem:measuregroup", "bioassay", "measuregroup");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("bao:BAO_0000040"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:source"),
                    config.createIriMapping("pubchem:source", "source"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:title"),
                    config.createLiteralMapping(xsdString, "title"));
            config.addQuadMapping(table, graph, config.createIriMapping("pubchem:bioassay", "bioassay"),
                    config.createIriMapping("bao:BAO_0000209"), subject);

            // extension
            config.addQuadMapping(table, graph, subject, config.createIriMapping("template:itemTemplate"),
                    config.createLiteralMapping("pubchem/Measuregroup.vm"));
        }

        {
            Table table = new Table(schema, "endpoint_bases");
            NodeMapping subject = config.createIriMapping("pubchem:measuregroup", "bioassay", "measuregroup");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("obo:OBI_0000299"),
                    config.createIriMapping("pubchem:endpoint", "substance", "bioassay", "measuregroup"));
        }

        {
            Table table = new Table(schema, "measuregroup_genes");
            NodeMapping subject = config.createIriMapping("pubchem:measuregroup", "bioassay", "measuregroup");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("obo:RO_0000057"),
                    config.createIriMapping("pubchem:gene", "gene"));
        }

        {
            Table table = new Table(schema, "measuregroup_proteins");
            NodeMapping subject = config.createIriMapping("pubchem:measuregroup", "bioassay", "measuregroup");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("obo:RO_0000057"),
                    config.createIriMapping("pubchem:protein", "protein"));
        }

        {
            Table table = new Table(schema, "measuregroup_taxonomies");
            NodeMapping subject = config.createIriMapping("pubchem:measuregroup", "bioassay", "measuregroup");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("obo:RO_0000057"),
                    config.createIriMapping("pubchem:taxonomy", "taxonomy"));
        }

        {
            Table table = new Table(schema, "measuregroup_cells");
            NodeMapping subject = config.createIriMapping("pubchem:measuregroup", "bioassay", "measuregroup");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("obo:RO_0000057"),
                    config.createIriMapping("pubchem:cell", "cell"));
        }
    }
}
