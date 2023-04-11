package cz.iocb.chemweb.server.sparql.config.pubchem;

import static cz.iocb.chemweb.server.sparql.config.pubchem.PubChemConfiguration.schema;
import static cz.iocb.chemweb.server.sparql.mapping.classes.BuiltinClasses.xsdString;
import cz.iocb.chemweb.server.sparql.config.SparqlDatabaseConfiguration;
import cz.iocb.chemweb.server.sparql.config.ontology.Ontology;
import cz.iocb.chemweb.server.sparql.database.Table;
import cz.iocb.chemweb.server.sparql.database.TableColumn;
import cz.iocb.chemweb.server.sparql.mapping.ConstantIriMapping;
import cz.iocb.chemweb.server.sparql.mapping.NodeMapping;
import cz.iocb.chemweb.server.sparql.mapping.classes.MapUserIriClass;



public class Synonym
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(new MapUserIriClass("pubchem:synonym", "integer", new Table(schema, "synonym_bases"),
                new TableColumn("id"), new TableColumn("md5"), "http://rdf.ncbi.nlm.nih.gov/pubchem/synonym/MD5_"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("pubchem:synonym");

        {
            Table table = new Table(schema, "synonym_bases");
            NodeMapping subject = config.createIriMapping("pubchem:synonym", "id");

            // extension
            config.addQuadMapping(table, graph, subject, config.createIriMapping("template:itemTemplate"),
                    config.createLiteralMapping("pubchem/Synonym.vm"));
        }

        {
            Table table = new Table(schema, "synonym_values");
            NodeMapping subject = config.createIriMapping("pubchem:synonym", "synonym");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(xsdString, "value"));

            // deprecated
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:has-value"),
                    config.createLiteralMapping(xsdString, "value"));
        }

        {
            Table table = new Table(schema, "synonym_types");
            NodeMapping subject = config.createIriMapping("pubchem:synonym", "synonym");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("ontology:resource", Ontology.unitCHEMINF, "type_id"));
        }

        {
            Table table = new Table(schema, "synonym_compounds");
            NodeMapping subject = config.createIriMapping("pubchem:synonym", "synonym");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000011"),
                    config.createIriMapping("pubchem:compound", "compound"));

            // deprecated
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:is-attribute-of"),
                    config.createIriMapping("pubchem:compound", "compound"));
        }

        {
            Table table = new Table(schema, "synonym_mesh_subjects");
            NodeMapping subject = config.createIriMapping("pubchem:synonym", "synonym");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:subject"),
                    config.createIriMapping("mesh:heading", "subject"));
        }

        {
            Table table = new Table(schema, "synonym_concept_subjects");
            NodeMapping subject = config.createIriMapping("pubchem:synonym", "synonym");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:subject"),
                    config.createIriMapping("pubchem:concept", "concept"));
        }
    }
}
