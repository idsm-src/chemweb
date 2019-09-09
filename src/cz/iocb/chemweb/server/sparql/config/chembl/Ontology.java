package cz.iocb.chemweb.server.sparql.config.chembl;

import static cz.iocb.chemweb.server.sparql.mapping.classes.BuiltinClasses.xsdString;
import java.util.Arrays;
import cz.iocb.chemweb.server.sparql.mapping.ConstantIriMapping;
import cz.iocb.chemweb.server.sparql.mapping.IriMapping;
import cz.iocb.chemweb.server.sparql.mapping.NodeMapping;
import cz.iocb.chemweb.server.sparql.mapping.classes.UserIntBlankNodeClass;
import cz.iocb.chemweb.server.sparql.mapping.classes.UserIriClass;
import cz.iocb.chemweb.server.sparql.mapping.classes.UserIriClass.SqlCheck;



class Ontology
{
    static void addIriClasses(ChemblConfiguration config)
    {
        config.addIriClass(new UserIriClass("ontology_term", Arrays.asList("integer"),
                "(http://rdf\\.ebi\\.ac\\.uk/terms/chembl#.*)|(http://purl\\.org/dc/terms/(description|title|date))|"
                        + "(http://www\\.w3\\.org/2001/XMLSchema#(boolean|float|integer|string))",
                SqlCheck.IF_MATCH));
    }


    static void addQuadMapping(ChemblConfiguration config)
    {
        UserIriClass term = config.getIriClass("ontology_term");
        UserIntBlankNodeClass list = new UserIntBlankNodeClass(1);
        UserIntBlankNodeClass domain = new UserIntBlankNodeClass(2);
        ConstantIriMapping graph = config.createIriMapping("<http://rdf.ebi.ac.uk/dataset/chembl>");


        String table = "ontology_terms";
        NodeMapping subject = config.createIriMapping(term, "term_id");

        config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                config.createIriMapping("owl:Class"), "type = 'CLASS'");

        config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                config.createIriMapping("owl:DatatypeProperty"), "type = 'DATATYPE PROPERTY'");

        config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                config.createIriMapping("owl:ObjectProperty"), "type = 'OBJECT PROPERTY'");

        config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:label"),
                config.createLiteralMapping(xsdString, "label"));

        config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:prefLabel"),
                config.createLiteralMapping(xsdString, "pref_label"));

        config.addQuadMapping(table, graph, subject, config.createIriMapping("dc:description"),
                config.createLiteralMapping(xsdString, "description"));


        config.addQuadMapping("ontology_parent_classes", graph, subject, config.createIriMapping("rdfs:subClassOf"),
                config.createIriMapping("ontology_term", "parent_term_id"));


        config.addQuadMapping("ontology_parent_properties", graph, subject,
                config.createIriMapping("rdfs:subPropertyOf"),
                config.createIriMapping("ontology_term", "parent_term_id"));


        config.addQuadMapping("ontology_domains", graph, subject, config.createIriMapping("rdfs:domain"),
                config.createIriMapping("ontology_term", "domain_term_id"));


        config.addQuadMapping("ontology_union_domains", graph, subject, config.createIriMapping("rdfs:domain"),
                config.createBlankNodeMapping(domain, "term_id"));

        config.addQuadMapping("ontology_union_domains", graph, config.createBlankNodeMapping(domain, "term_id"),
                config.createIriMapping("rdf:type"), config.createIriMapping("owl:Class"));

        config.addQuadMapping("ontology_union_domains", graph, config.createBlankNodeMapping(domain, "term_id"),
                config.createIriMapping("owl:unionOf"), config.createBlankNodeMapping(list, "list_id"));


        config.addQuadMapping("ontology_ranges", graph, subject, config.createIriMapping("rdfs:range"),
                config.createIriMapping("ontology_term", "range_term_id"));


        config.addQuadMapping("ontology_union_lists", graph, config.createBlankNodeMapping(list, "list_id"),
                config.createIriMapping("rdf:rest"), config.createBlankNodeMapping(list, "rest_list_id"));

        config.addQuadMapping("ontology_union_lists", graph, config.createBlankNodeMapping(list, "list_id"),
                config.createIriMapping("rdf:rest"), config.createIriMapping("rdf:nil"), "rest_list_id is null");

        config.addQuadMapping("ontology_union_lists", graph, config.createBlankNodeMapping(list, "list_id"),
                config.createIriMapping("rdf:first"), subject);


        IriMapping chembl = config.createIriMapping("<http://rdf.ebi.ac.uk/terms/chembl>");

        config.addQuadMapping(null, graph, chembl, config.createIriMapping("rdf:type"),
                config.createIriMapping("owl:Ontology"));
        config.addQuadMapping(null, graph, chembl, config.createIriMapping("owl:imports"),
                config.createIriMapping("dc:"));
        config.addQuadMapping(null, graph, chembl, config.createIriMapping("owl:imports"),
                config.createIriMapping("<http://purl.org/dc/terms/>"));
        config.addQuadMapping(null, graph, chembl, config.createIriMapping("owl:imports"),
                config.createIriMapping("<http://semanticscience.org/ontology/sio.owl>"));
        config.addQuadMapping(null, graph, chembl, config.createIriMapping("owl:imports"),
                config.createIriMapping("<http://www.w3.org/2004/02/skos/core>"));
        config.addQuadMapping(null, graph, chembl, config.createIriMapping("owl:imports"),
                config.createIriMapping("<http://xmlns.com/foaf/0.1/>"));
        config.addQuadMapping(null, graph, chembl, config.createIriMapping("dc:description"),
                config.createLiteralMapping(
                        "ChEMBL Core Ontology is used to describe the define entities, which appear in the ChEMBL-RDF"));
        config.addQuadMapping(null, graph, chembl, config.createIriMapping("dc:title"),
                config.createLiteralMapping("ChEMBL Core Ontology"));
        config.addQuadMapping(null, graph, chembl, config.createIriMapping("owl:versionInfo"),
                config.createLiteralMapping("Created with TopBraid Composer"));
    }
}