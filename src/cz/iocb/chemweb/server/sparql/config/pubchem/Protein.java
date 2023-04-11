package cz.iocb.chemweb.server.sparql.config.pubchem;

import static cz.iocb.chemweb.server.sparql.config.pubchem.PubChemConfiguration.rdfLangStringEn;
import static cz.iocb.chemweb.server.sparql.config.pubchem.PubChemConfiguration.schema;
import static cz.iocb.chemweb.server.sparql.mapping.classes.BuiltinClasses.xsdString;
import cz.iocb.chemweb.server.sparql.config.SparqlDatabaseConfiguration;
import cz.iocb.chemweb.server.sparql.config.ontology.Ontology;
import cz.iocb.chemweb.server.sparql.database.Table;
import cz.iocb.chemweb.server.sparql.database.TableColumn;
import cz.iocb.chemweb.server.sparql.mapping.ConstantIriMapping;
import cz.iocb.chemweb.server.sparql.mapping.NodeMapping;
import cz.iocb.chemweb.server.sparql.mapping.classes.MapUserIriClass;



public class Protein
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(new MapUserIriClass("pubchem:enzyme", "integer", new Table(schema, "enzyme_bases"),
                new TableColumn("id"), new TableColumn("iri"), "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/EC_"));
        config.addIriClass(new MapUserIriClass("pubchem:protein", "integer", new Table(schema, "protein_bases"),
                new TableColumn("id"), new TableColumn("iri"), "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/ACC"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("pubchem:protein");

        {
            Table table = new Table(schema, "enzyme_bases");
            NodeMapping subject = config.createIriMapping("pubchem:enzyme", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:SIO_010343"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("up:Enzyme"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:subClassOf"),
                    config.createIriMapping("pubchem:enzyme", "parent"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:subClassOf"),
                    config.createIriMapping("up:Enzyme"), config.createIsNullCondition("parent"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:sameAs"),
                    config.createIriMapping("purl:enzyme", "iri"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:prefLabel"),
                    config.createLiteralMapping(xsdString, "title"));

            // extension
            config.addQuadMapping(table, graph, subject, config.createIriMapping("template:itemTemplate"),
                    config.createLiteralMapping("pubchem/Enzyme.vm"));
        }

        {
            Table table = new Table(schema, "enzyme_alternatives");
            NodeMapping subject = config.createIriMapping("pubchem:enzyme", "enzyme");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:altLabel"),
                    config.createLiteralMapping(rdfLangStringEn, "alternative"));
        }

        {
            Table table = new Table(schema, "protein_bases");
            NodeMapping subject = config.createIriMapping("pubchem:protein", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:sameAs"),
                    config.createIriMapping("ncbi:protein", "iri"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:prefLabel"),
                    config.createLiteralMapping(xsdString, "title"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("up:organism"),
                    config.createIriMapping("pubchem:taxonomy", "organism"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0002817"),
                    config.createLiteralMapping(xsdString, "sequence"));

            // deprecated
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("bp:Protein"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:title"),
                    config.createLiteralMapping(xsdString, "title"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("bp:organism"),
                    config.createIriMapping("pubchem:taxonomy", "organism"));

            // extension
            config.addQuadMapping(table, graph, subject, config.createIriMapping("up:organism"),
                    config.createIriMapping("ontology:resource", Ontology.unitNCBITaxon, "organism"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("template:itemTemplate"),
                    config.createLiteralMapping("pubchem/Protein.vm"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("template:pageTemplate"),
                    config.createLiteralMapping("pubchem/Protein.vm"));

            // deprecated extension
            config.addQuadMapping(table, graph, subject, config.createIriMapping("bp:organism"),
                    config.createIriMapping("ontology:resource", Ontology.unitNCBITaxon, "organism"));
        }

        {
            Table table = new Table(schema, "protein_alternatives");
            NodeMapping subject = config.createIriMapping("pubchem:protein", "protein");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:altLabel"),
                    config.createLiteralMapping(rdfLangStringEn, "alternative"));
        }

        {
            Table table = new Table(schema, "protein_pdblinks");
            NodeMapping subject = config.createIriMapping("pubchem:protein", "protein");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("pdbo40:link_to_pdb"),
                    config.createIriMapping("rdf:wwpdb_old", "pdblink"));

            // extension
            config.addQuadMapping(table, graph, subject, config.createIriMapping("pdbo:link_to_pdb"),
                    config.createIriMapping("rdf:wwpdb", "pdblink"));
        }

        {
            Table table = new Table(schema, "protein_similarproteins");
            NodeMapping subject = config.createIriMapping("pubchem:protein", "protein");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("vocab:hasSimilarProtein"),
                    config.createIriMapping("pubchem:protein", "simprotein"));
        }

        {
            Table table = new Table(schema, "protein_genes");
            NodeMapping subject = config.createIriMapping("pubchem:protein", "protein");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("up:encodedBy"),
                    config.createIriMapping("pubchem:gene", "gene"));
        }

        {
            Table table = new Table(schema, "protein_uniprot_enzymes");
            NodeMapping subject = config.createIriMapping("pubchem:protein", "protein");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("up:enzyme"),
                    config.createIriMapping("purl:enzyme", "enzyme"));
        }

        {
            Table table = new Table(schema, "protein_enzymes");
            NodeMapping subject = config.createIriMapping("pubchem:protein", "protein");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("up:enzyme"),
                    config.createIriMapping("pubchem:enzyme", "enzyme"));
        }

        {
            Table table = new Table(schema, "protein_ncbi_matches");
            NodeMapping subject = config.createIriMapping("pubchem:protein", "protein");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:closeMatch"),
                    config.createIriMapping("ncbi:protein", "match"));
        }

        {
            Table table = new Table(schema, "protein_uniprot_matches");
            NodeMapping subject = config.createIriMapping("pubchem:protein", "protein");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:closeMatch"),
                    config.createIriMapping("purl:uniprot", "match"));
        }

        {
            Table table = new Table(schema, "protein_mesh_matches");
            NodeMapping subject = config.createIriMapping("pubchem:protein", "protein");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:closeMatch"),
                    config.createIriMapping("mesh:heading", "match"));
        }

        {
            Table table = new Table(schema, "protein_thesaurus_matches");
            NodeMapping subject = config.createIriMapping("pubchem:protein", "protein");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:closeMatch"),
                    config.createIriMapping("ontology:resource", Ontology.unitThesaurus, "match"));
        }

        {
            Table table = new Table(schema, "protein_guidetopharmacology_matches");
            NodeMapping subject = config.createIriMapping("pubchem:protein", "protein");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:closeMatch"),
                    config.createIriMapping("guidetopharmacology:target", "match"));
        }

        {
            Table table = new Table(schema, "protein_drugbank_matches");
            NodeMapping subject = config.createIriMapping("pubchem:protein", "protein");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:closeMatch"),
                    config.createIriMapping("drugbank:bio_entities", "match"));
        }

        {
            Table table = new Table(schema, "protein_chembl_matches");
            NodeMapping subject = config.createIriMapping("pubchem:protein", "protein");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:closeMatch"),
                    config.createIriMapping("chembl:target_report_card", "match"));
        }

        {
            Table table = new Table(schema, "protein_glygen_matches");
            NodeMapping subject = config.createIriMapping("pubchem:protein", "protein");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:closeMatch"),
                    config.createIriMapping("glygen:protein", "match"));
        }

        {
            Table table = new Table(schema, "protein_glycosmos_matches");
            NodeMapping subject = config.createIriMapping("pubchem:protein", "protein");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:closeMatch"),
                    config.createIriMapping("glycosmos:glycoproteins", "match"));
        }

        {
            Table table = new Table(schema, "protein_alphafold_matches");
            NodeMapping subject = config.createIriMapping("pubchem:protein", "protein");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:closeMatch"),
                    config.createIriMapping("alphafold:entry", "match"));
        }

        {
            Table table = new Table(schema, "protein_conserveddomains");
            NodeMapping subject = config.createIriMapping("pubchem:protein", "protein");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("obo:RO_0002180"),
                    config.createIriMapping("pubchem:conserveddomain", "domain"));
        }

        {
            Table table = new Table(schema, "protein_continuantparts");
            NodeMapping subject = config.createIriMapping("pubchem:protein", "protein");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("obo:RO_0002180"),
                    config.createIriMapping("pubchem:protein", "part"));
        }

        {
            Table table = new Table(schema, "protein_families");
            NodeMapping subject = config.createIriMapping("pubchem:protein", "protein");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("obo:RO_0002180"),
                    config.createIriMapping("pfam:family", "family"));
        }

        {
            Table table = new Table(schema, "protein_types");
            NodeMapping subject = config.createIriMapping("pubchem:protein", "protein");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("ontology:resource", "type_unit", "type_id"));
        }
    }
}
