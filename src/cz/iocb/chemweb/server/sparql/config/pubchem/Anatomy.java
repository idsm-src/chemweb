package cz.iocb.chemweb.server.sparql.config.pubchem;

import static cz.iocb.chemweb.server.sparql.config.pubchem.PubChemConfiguration.schema;
import static cz.iocb.chemweb.server.sparql.mapping.classes.BuiltinClasses.xsdString;
import cz.iocb.chemweb.server.sparql.config.SparqlDatabaseConfiguration;
import cz.iocb.chemweb.server.sparql.config.ontology.Ontology;
import cz.iocb.chemweb.server.sparql.database.Table;
import cz.iocb.chemweb.server.sparql.mapping.ConstantIriMapping;
import cz.iocb.chemweb.server.sparql.mapping.NodeMapping;
import cz.iocb.chemweb.server.sparql.mapping.classes.IntegerUserIriClass;



public class Anatomy
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(new IntegerUserIriClass("pubchem:anatomy", "integer",
                "http://rdf.ncbi.nlm.nih.gov/pubchem/anatomy/ANATOMYID"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("pubchem:anatomy");

        {
            Table table = new Table(schema, "anatomy_bases");
            NodeMapping subject = config.createIriMapping("pubchem:anatomy", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:SIO_001262"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("ontology:resource", Ontology.unitUberon, "id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:prefLabel"),
                    config.createLiteralMapping(xsdString, "label"));

            // extension
            config.addQuadMapping(table, graph, subject, config.createIriMapping("template:itemTemplate"),
                    config.createLiteralMapping("pubchem/Anatomy.vm"));
        }

        {
            Table table = new Table(schema, "anatomy_alternatives");
            NodeMapping subject = config.createIriMapping("pubchem:anatomy", "anatomy");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:altLabel"),
                    config.createLiteralMapping(xsdString, "alternative"));
        }

        {
            Table table = new Table(schema, "anatomy_matches");
            NodeMapping subject = config.createIriMapping("pubchem:anatomy", "anatomy");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:closeMatch"),
                    config.createIriMapping("ontology:resource", "match_unit", "match_id"));
        }

        {
            Table table = new Table(schema, "anatomy_chembl_matches");
            NodeMapping subject = config.createIriMapping("pubchem:anatomy", "anatomy");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:closeMatch"),
                    config.createIriMapping("chembl:tissue_report_card", "match"));
        }

        {
            Table table = new Table(schema, "anatomy_nextprot_matches");
            NodeMapping subject = config.createIriMapping("pubchem:anatomy", "anatomy");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:closeMatch"),
                    config.createIriMapping("nextprot:term", "match"));
        }

        {
            Table table = new Table(schema, "anatomy_mesh_matches");
            NodeMapping subject = config.createIriMapping("pubchem:anatomy", "anatomy");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:closeMatch"),
                    config.createIriMapping("mesh:heading", "match"));
        }
    }
}
