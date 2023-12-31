package cz.iocb.chemweb.server.sparql.config.pubchem;

import static cz.iocb.chemweb.server.sparql.config.pubchem.PubChemConfiguration.schema;
import static cz.iocb.chemweb.server.sparql.mapping.classes.BuiltinClasses.xsdString;
import cz.iocb.chemweb.server.sparql.config.SparqlDatabaseConfiguration;
import cz.iocb.chemweb.server.sparql.config.ontology.Ontology;
import cz.iocb.chemweb.server.sparql.database.Table;
import cz.iocb.chemweb.server.sparql.mapping.ConstantIriMapping;
import cz.iocb.chemweb.server.sparql.mapping.NodeMapping;
import cz.iocb.chemweb.server.sparql.mapping.classes.IntegerUserIriClass;
import cz.iocb.chemweb.server.sparql.mapping.classes.StringUserIriClass;



public class Pathway
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(new IntegerUserIriClass("pubchem:pathway", "integer",
                "http://rdf.ncbi.nlm.nih.gov/pubchem/pathway/PWID"));
        config.addIriClass(new StringUserIriClass("pubchem:reaction", "http://rdf.ncbi.nlm.nih.gov/pubchem/reaction/"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("pubchem:pathway");

        {
            Table table = new Table(schema, "pathway_bases");
            NodeMapping subject = config.createIriMapping("pubchem:pathway", "id");
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("bp:Pathway"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:title"),
                    config.createLiteralMapping(xsdString, "title"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:source"),
                    config.createIriMapping("pubchem:source", "source"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("up:organism"),
                    config.createIriMapping("pubchem:taxonomy", "organism"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:sameAs"),
                    config.createIriMapping("reference:pathbank-pathway", "reference"), config.createAreEqualCondition(
                            "reference_type", "'PATHBANK'::" + schema + ".pathway_reference_type"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:sameAs"),
                    config.createIriMapping("reference:biocyc-image-pathway", "reference"),
                    config.createAreEqualCondition("reference_type",
                            "'BIOCYC_IMAGE'::" + schema + ".pathway_reference_type"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:sameAs"),
                    config.createIriMapping("identifiers:reactome", "reference"), config.createAreEqualCondition(
                            "reference_type", "'REACTOME'::" + schema + ".pathway_reference_type"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:sameAs"),
                    config.createIriMapping("identifiers:wikipathway", "reference"), config.createAreEqualCondition(
                            "reference_type", "'WIKIPATHWAY'::" + schema + ".pathway_reference_type"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:sameAs"),
                    config.createIriMapping("reference:biocyc-pathway", "reference"), config.createAreEqualCondition(
                            "reference_type", "'BIOCYC'::" + schema + ".pathway_reference_type"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:sameAs"),
                    config.createIriMapping("reference:plantcyc-pathway", "reference"), config.createAreEqualCondition(
                            "reference_type", "'PLANTCYC'::" + schema + ".pathway_reference_type"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:sameAs"),
                    config.createIriMapping("reference:pid-pathway", "reference"),
                    config.createAreEqualCondition("reference_type", "'PID'::" + schema + ".pathway_reference_type"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:sameAs"),
                    config.createIriMapping("reference:inoh-pathway", "reference"),
                    config.createAreEqualCondition("reference_type", "'INOH'::" + schema + ".pathway_reference_type"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:sameAs"),
                    config.createIriMapping("reference:plantreactome-pathway", "reference"),
                    config.createAreEqualCondition("reference_type",
                            "'PLANTREACTOME'::" + schema + ".pathway_reference_type"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:sameAs"),
                    config.createIriMapping("reference:pharmgkb-pathway", "reference"), config.createAreEqualCondition(
                            "reference_type", "'PHARMGKB'::" + schema + ".pathway_reference_type"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:sameAs"),
                    config.createIriMapping("reference:fairdomhub-model", "reference"), config.createAreEqualCondition(
                            "reference_type", "'FAIRDOMHUB'::" + schema + ".pathway_reference_type"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:sameAs"),
                    config.createIriMapping("reference:lipidmaps-pathway", "reference"), config.createAreEqualCondition(
                            "reference_type", "'LIPIDMAPS'::" + schema + ".pathway_reference_type"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:sameAs"),
                    config.createIriMapping("reference:pantherdb-pathway", "reference"), config.createAreEqualCondition(
                            "reference_type", "'PANTHERDB'::" + schema + ".pathway_reference_type"));

            // deprecated
            config.addQuadMapping(table, graph, subject, config.createIriMapping("bp:organism"),
                    config.createIriMapping("pubchem:taxonomy", "organism"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:exactMatch"),
                    config.createIriMapping("reference:pathbank-pathway", "reference"), config.createAreEqualCondition(
                            "reference_type", "'PATHBANK'::" + schema + ".pathway_reference_type"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:exactMatch"),
                    config.createIriMapping("reference:biocyc-image-pathway", "reference"),
                    config.createAreEqualCondition("reference_type",
                            "'BIOCYC_IMAGE'::" + schema + ".pathway_reference_type"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:exactMatch"),
                    config.createIriMapping("identifiers:reactome", "reference"), config.createAreEqualCondition(
                            "reference_type", "'REACTOME'::" + schema + ".pathway_reference_type"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:exactMatch"),
                    config.createIriMapping("identifiers:wikipathway", "reference"), config.createAreEqualCondition(
                            "reference_type", "'WIKIPATHWAY'::" + schema + ".pathway_reference_type"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:exactMatch"),
                    config.createIriMapping("reference:biocyc-pathway", "reference"), config.createAreEqualCondition(
                            "reference_type", "'BIOCYC'::" + schema + ".pathway_reference_type"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:exactMatch"),
                    config.createIriMapping("reference:plantcyc-pathway", "reference"), config.createAreEqualCondition(
                            "reference_type", "'PLANTCYC'::" + schema + ".pathway_reference_type"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:exactMatch"),
                    config.createIriMapping("reference:pid-pathway", "reference"),
                    config.createAreEqualCondition("reference_type", "'PID'::" + schema + ".pathway_reference_type"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:exactMatch"),
                    config.createIriMapping("reference:inoh-pathway", "reference"),
                    config.createAreEqualCondition("reference_type", "'INOH'::" + schema + ".pathway_reference_type"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:exactMatch"),
                    config.createIriMapping("reference:plantreactome-pathway", "reference"),
                    config.createAreEqualCondition("reference_type",
                            "'PLANTREACTOME'::" + schema + ".pathway_reference_type"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:exactMatch"),
                    config.createIriMapping("reference:pharmgkb-pathway", "reference"), config.createAreEqualCondition(
                            "reference_type", "'PHARMGKB'::" + schema + ".pathway_reference_type"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:exactMatch"),
                    config.createIriMapping("reference:fairdomhub-model", "reference"), config.createAreEqualCondition(
                            "reference_type", "'FAIRDOMHUB'::" + schema + ".pathway_reference_type"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:exactMatch"),
                    config.createIriMapping("reference:lipidmaps-pathway", "reference"), config.createAreEqualCondition(
                            "reference_type", "'LIPIDMAPS'::" + schema + ".pathway_reference_type"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:exactMatch"),
                    config.createIriMapping("reference:pantherdb-pathway", "reference"), config.createAreEqualCondition(
                            "reference_type", "'PANTHERDB'::" + schema + ".pathway_reference_type"));

            // extension
            config.addQuadMapping(table, graph, subject, config.createIriMapping("template:itemTemplate"),
                    config.createLiteralMapping("pubchem/Pathway.vm"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("template:pageTemplate"),
                    config.createLiteralMapping("pubchem/Pathway.vm"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("up:organism"),
                    config.createIriMapping("ontology:resource", Ontology.unitNCBITaxon, "organism"));

            // deprecated extension
            config.addQuadMapping(table, graph, subject, config.createIriMapping("bp:organism"),
                    config.createIriMapping("ontology:resource", Ontology.unitNCBITaxon, "organism"));
        }

        {
            Table table = new Table(schema, "pathway_compounds");
            NodeMapping subject = config.createIriMapping("pubchem:pathway", "pathway");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("obo:RO_0000057"),
                    config.createIriMapping("pubchem:compound", "compound"));
        }

        {
            Table table = new Table(schema, "pathway_proteins");
            NodeMapping subject = config.createIriMapping("pubchem:pathway", "pathway");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("obo:RO_0000057"),
                    config.createIriMapping("pubchem:protein", "protein"));
        }

        {
            Table table = new Table(schema, "pathway_genes");
            NodeMapping subject = config.createIriMapping("pubchem:pathway", "pathway");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("obo:RO_0000057"),
                    config.createIriMapping("pubchem:gene", "gene"));
        }

        {
            Table table = new Table(schema, "pathway_components");
            NodeMapping subject = config.createIriMapping("pubchem:pathway", "pathway");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bp:pathwayComponent"),
                    config.createIriMapping("pubchem:pathway", "component"));
        }

        {
            Table table = new Table(schema, "pathway_references");
            NodeMapping subject = config.createIriMapping("pubchem:pathway", "pathway");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cito:isDiscussedBy"),
                    config.createIriMapping("pubchem:reference", "reference"));
        }

        {
            Table table = new Table(schema, "pathway_related_pathways");
            NodeMapping subject = config.createIriMapping("pubchem:pathway", "pathway");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:related"),
                    config.createIriMapping("pubchem:pathway", "related"));
        }
    }
}
