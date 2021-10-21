package cz.iocb.chemweb.server.sparql.config.pubchem;

import static cz.iocb.chemweb.server.sparql.config.pubchem.PubChemConfiguration.rdfLangStringEn;
import static cz.iocb.chemweb.server.sparql.config.pubchem.PubChemConfiguration.schema;
import cz.iocb.chemweb.server.sparql.config.SparqlDatabaseConfiguration;
import cz.iocb.chemweb.server.sparql.config.ontology.Ontology;
import cz.iocb.chemweb.server.sparql.database.Table;
import cz.iocb.chemweb.server.sparql.mapping.ConstantIriMapping;
import cz.iocb.chemweb.server.sparql.mapping.NodeMapping;
import cz.iocb.chemweb.server.sparql.mapping.classes.IntegerUserIriClass;
import cz.iocb.chemweb.server.sparql.translator.imcode.SqlTableAccess.Condition;



public class Bioassay
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(new IntegerUserIriClass("pubchem:bioassay", "integer",
                "http://rdf.ncbi.nlm.nih.gov/pubchem/bioassay/AID"));
        config.addIriClass(new IntegerUserIriClass("pubchem:bioassay_description", "integer",
                "http://rdf.ncbi.nlm.nih.gov/pubchem/bioassay/AID", "_Description"));
        config.addIriClass(new IntegerUserIriClass("pubchem:bioassay_protocol", "integer",
                "http://rdf.ncbi.nlm.nih.gov/pubchem/bioassay/AID", "_Protocol"));
        config.addIriClass(new IntegerUserIriClass("pubchem:bioassay_comment", "integer",
                "http://rdf.ncbi.nlm.nih.gov/pubchem/bioassay/AID", "_Comment"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("pubchem:bioassay");

        {
            Table table = new Table(schema, "bioassay_bases");
            NodeMapping subject = config.createIriMapping("pubchem:bioassay", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("bao:BAO_0000015"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:title"),
                    config.createLiteralMapping(rdfLangStringEn, "title"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:source"),
                    config.createIriMapping("pubchem:source", "source"));

            // extension
            config.addQuadMapping(table, graph, subject, config.createIriMapping("template:itemTemplate"),
                    config.createLiteralMapping("pubchem/Bioassay.vm"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("template:pageTemplate"),
                    config.createLiteralMapping("pubchem/Bioassay.vm"));
        }

        {
            Table table = new Table(schema, "bioassay_stages");
            NodeMapping subject = config.createIriMapping("pubchem:bioassay", "bioassay");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0000210"),
                    config.createIriMapping("ontology:resource", Ontology.unitBAO, "stage"));
        }

        {
            Table table = new Table(schema, "bioassay_confirmatory_assays");
            NodeMapping subject = config.createIriMapping("pubchem:bioassay", "bioassay");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0000540"),
                    config.createIriMapping("pubchem:bioassay", "confirmatory_assay"));
        }

        {
            Table table = new Table(schema, "bioassay_primary_assays");
            NodeMapping subject = config.createIriMapping("pubchem:bioassay", "bioassay");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0001067"),
                    config.createIriMapping("pubchem:bioassay", "primary_assay"));
        }

        {
            Table table = new Table(schema, "bioassay_summary_assays");
            NodeMapping subject = config.createIriMapping("pubchem:bioassay", "bioassay");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0001094"),
                    config.createIriMapping("pubchem:bioassay", "summary_assay"));
        }

        // extensions
        {
            Table table = new Table(schema, "bioassay_chembl_assays");
            NodeMapping subject = config.createIriMapping("pubchem:bioassay", "bioassay");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:exactMatch"),
                    config.createIriMapping("chembl:assay", "chembl_assay"));
        }

        {
            Table table = new Table(schema, "bioassay_chembl_mechanisms");
            NodeMapping subject = config.createIriMapping("pubchem:bioassay", "bioassay");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:exactMatch"),
                    config.createIriMapping("chembl:mechanism", "chembl_mechanism"));
        }

        {
            Table table = new Table(schema, "bioassay_data");
            NodeMapping subject = config.createIriMapping("pubchem:bioassay_description", "bioassay");
            Condition condition = config.createAreEqualCondition("type_id", "'136'::smallint");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cheminf:SIO_000136"), condition);
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:is-attribute-of"),
                    config.createIriMapping("pubchem:bioassay", "bioassay"), condition);
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:has-value"),
                    config.createLiteralMapping(rdfLangStringEn, "value"), condition);
        }

        {
            Table table = new Table(schema, "bioassay_data");
            NodeMapping subject = config.createIriMapping("pubchem:bioassay_protocol", "bioassay");
            Condition condition = config.createAreEqualCondition("type_id", "'1041'::smallint");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cheminf:SIO_001041"), condition);
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:is-attribute-of"),
                    config.createIriMapping("pubchem:bioassay", "bioassay"), condition);
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:has-value"),
                    config.createLiteralMapping(rdfLangStringEn, "value"), condition);
        }

        {
            Table table = new Table(schema, "bioassay_data");
            NodeMapping subject = config.createIriMapping("pubchem:bioassay_comment", "bioassay");
            Condition condition = config.createAreEqualCondition("type_id", "'1167'::smallint");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cheminf:SIO_001167"), condition);
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:is-attribute-of"),
                    config.createIriMapping("pubchem:bioassay", "bioassay"), condition);
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:has-value"),
                    config.createLiteralMapping(rdfLangStringEn, "value"), condition);
        }
    }
}
