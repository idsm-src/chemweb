package cz.iocb.chemweb.server.servlets.endpoint;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import cz.iocb.chemweb.server.sparql.config.SparqlDatabaseConfiguration;
import cz.iocb.chemweb.server.sparql.engine.BNode;
import cz.iocb.chemweb.server.sparql.engine.Engine;
import cz.iocb.chemweb.server.sparql.engine.IriNode;
import cz.iocb.chemweb.server.sparql.engine.LanguageTaggedLiteral;
import cz.iocb.chemweb.server.sparql.engine.LiteralNode;
import cz.iocb.chemweb.server.sparql.engine.RdfNode;
import cz.iocb.chemweb.server.sparql.engine.Request;
import cz.iocb.chemweb.server.sparql.engine.Result;
import cz.iocb.chemweb.server.sparql.engine.Result.ResultType;
import cz.iocb.chemweb.server.sparql.engine.TypedLiteral;
import cz.iocb.chemweb.server.sparql.error.TranslateExceptions;
import cz.iocb.chemweb.server.sparql.parser.model.DataSet;
import cz.iocb.chemweb.server.sparql.parser.model.IRI;



@SuppressWarnings("serial")
public class EndpointServlet extends HttpServlet
{
    static enum OutputType
    {
        NONE(""),
        SPARQL_XML("application/sparql-results+xml", ResultType.SELECT, ResultType.ASK),
        SPARQL_JSON("application/sparql-results+json", ResultType.SELECT, ResultType.ASK),
        RDF_XML("application/rdf+xml"/*, ResultType.DESCRIBE, ResultType.CONSTRUCT*/),
        RDF_JSON("application/rdf+json"/*, ResultType.DESCRIBE, ResultType.CONSTRUCT*/),
        NTRIPLES("application/n-triples"/*, ResultType.DESCRIBE, ResultType.CONSTRUCT*/),
        NQUADS("application/n-quads"/*, ResultType.DESCRIBE, ResultType.CONSTRUCT*/),
        TRIG("application/trig"/*, ResultType.DESCRIBE, ResultType.CONSTRUCT*/),
        TURTLE("text/turtle"/*, ResultType.DESCRIBE, ResultType.CONSTRUCT*/),
        TSV("text/tab-separated-values", ResultType.SELECT, ResultType.ASK, ResultType.DESCRIBE, ResultType.CONSTRUCT),
        CSV("text/csv", ResultType.SELECT, ResultType.ASK, ResultType.DESCRIBE, ResultType.CONSTRUCT);

        private final String mime;
        private final ResultType[] variants;

        private OutputType(String mime, ResultType... variants)
        {
            this.mime = mime;
            this.variants = variants;
        }

        public static OutputType getOutputType(String mime, ResultType variant)
        {
            for(OutputType value : OutputType.values())
            {
                if(value.mime.equals(mime))
                    for(ResultType v : value.variants)
                        if(v == variant)
                            return value;
            }

            return NONE;
        }
    }


    private Engine engine;


    @Override
    public void init(ServletConfig config) throws ServletException
    {
        String resourceName = config.getInitParameter("resource");

        if(resourceName == null || resourceName.isEmpty())
            throw new IllegalArgumentException("Resource name is not set");


        try
        {
            Context context = (Context) (new InitialContext()).lookup("java:comp/env");
            SparqlDatabaseConfiguration sparqlConfig = (SparqlDatabaseConfiguration) context.lookup(resourceName);

            engine = new Engine(sparqlConfig);
        }
        catch(NamingException e)
        {
            throw new ServletException(e);
        }
    }


    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException
    {
        res.setHeader("access-control-allow-headers",
                "x-requested-with, Content-Type, origin, authorization, accept, client-security-token");
        res.setHeader("access-control-allow-origin", "*");
        super.doOptions(req, res);
    }


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException
    {
        String query = req.getParameter("query");
        String[] defaultGraphs = req.getParameterValues("default-graph-uri");
        String[] namedGraphs = req.getParameterValues("named-graph-uri");

        process(req, res, query, defaultGraphs, namedGraphs);
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException
    {
        String query = null;
        String[] defaultGraphs = req.getParameterValues("default-graph-uri");
        String[] namedGraphs = req.getParameterValues("named-graph-uri");

        if(req.getContentType().matches("application/x-www-form-urlencoded.*"))
            query = req.getParameter("query");
        else if(req.getContentType().matches("application/sparql-query.*"))
            query = IOUtils.toString(req.getInputStream());

        process(req, res, query, defaultGraphs, namedGraphs);
    }


    public void process(HttpServletRequest req, HttpServletResponse res, String query, String[] defaultGraphs,
            String[] namedGraphs) throws IOException
    {
        if(query == null)
        {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }


        List<DataSet> dataSets = new ArrayList<>();

        try
        {
            if(defaultGraphs != null)
                for(String defaultGraph : defaultGraphs)
                    dataSets.add(new DataSet(new IRI(defaultGraph), true));

            if(namedGraphs != null)
                for(String namedGraph : namedGraphs)
                    dataSets.add(new DataSet(new IRI(namedGraph), false));
        }
        catch(IllegalArgumentException e)
        {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }


        res.setHeader("access-control-allow-headers",
                "x-requested-with, Content-Type, origin, authorization, accept, client-security-token");
        res.setHeader("access-control-allow-origin", "*");


        // IOCB SPARQL protocol extension
        String warnings = req.getParameter("warnings");
        String filename = req.getParameter("filename");

        if(filename != null)
            res.setHeader("content-disposition", "attachment; filename=\"" + filename + "\"");


        try
        {
            boolean includeWarnings = warnings != null ? Boolean.parseBoolean(warnings) : false;

            try(Request request = engine.getRequest())
            {
                try(Result result = request.execute(query, dataSets))
                {
                    switch(result.getResultType())
                    {
                        case ASK:
                            switch(detectOutputType(req, ResultType.ASK))
                            {
                                case SPARQL_JSON:
                                    writeAskJson(res, result, includeWarnings);
                                    break;
                                case SPARQL_XML:
                                    writeAskXml(res, result, includeWarnings);
                                    break;
                                case TSV:
                                    // non-standard extension
                                    writeAskTsv(res, result);
                                    break;
                                case CSV:
                                    // non-standard extension
                                    writeAskCsv(res, result);
                                    break;
                                default:
                                    res.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
                            }
                            break;

                        case SELECT:
                            switch(detectOutputType(req, ResultType.SELECT))
                            {
                                case SPARQL_JSON:
                                    writeSelectJson(res, result, includeWarnings);
                                    break;
                                case SPARQL_XML:
                                    writeSelectXml(res, result, includeWarnings);
                                    break;
                                case TSV:
                                    writeSelectTsv(res, result);
                                    break;
                                case CSV:
                                    writeSelectCsv(res, result);
                                    break;
                                default:
                                    res.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
                            }
                            break;

                        case DESCRIBE:
                        case CONSTRUCT:
                            switch(detectOutputType(req, ResultType.CONSTRUCT))
                            {
                                case TSV:
                                    writeSelectTsv(res, result);
                                    break;
                                case CSV:
                                    writeSelectCsv(res, result);
                                    break;
                                default:
                                    res.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
                            }
                            break;
                    }
                }
            }
        }
        catch(TranslateExceptions e)
        {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        catch(Throwable e)
        {
            System.err.println("EndpointServlet: log begin");

            if(defaultGraphs != null)
                for(String defaultGraph : defaultGraphs)
                    System.err.println("default graph: " + defaultGraph);

            if(namedGraphs != null)
                for(String namedGraph : namedGraphs)
                    System.err.println("named graph: " + namedGraph);

            System.err.println(query);
            e.printStackTrace(System.err);
            System.err.println("EndpointServlet: log end");

            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
    }


    private static OutputType detectOutputType(HttpServletRequest req, ResultType form)
    {
        // IOCB SPARQL protocol extension
        String accepts = req.getParameter("format");

        if(accepts == null)
        {
            accepts = req.getHeader("accept");

            if(accepts == null)
            {
                if(form == ResultType.ASK || form == ResultType.SELECT)
                    return OutputType.SPARQL_XML;
                else
                    return OutputType.TSV;
            }
        }


        OutputType type = OutputType.NONE;
        double quality = 0;

        // to not split around a comma inside quotation marks
        accepts = accepts.replaceAll("\"[^\"]*\"", "");

        for(String value : accepts.split("[\t ]*,[\t ]*"))
        {
            String[] parts = value.split("[\t ]*;[\t ]*");

            if(parts.length == 0)
                continue;

            double qvalue = 1;

            for(int i = 1; i < parts.length; i++)
                if(parts[i].matches("q=(0(\\.[0-9]{0,3})?|1(\\.0{0,3})?)[\\t ]*"))
                    qvalue = Double.parseDouble(parts[i].substring(2).replaceAll("[\t ]", ""));

            if(qvalue == 0)
                continue;

            String mime = parts[0].replaceAll("[\t ]", "");
            OutputType detected = OutputType.getOutputType(mime, form);

            if(detected != OutputType.NONE && qvalue >= quality)
            {
                type = detected;
                quality = qvalue;
            }
            else if(mime.equals("text/*") && qvalue > quality)
            {
                quality = qvalue;
                type = OutputType.TSV;
            }
            else if((mime.equals("*") || mime.equals("*/*") || mime.equals("application/*")) && qvalue > quality)
            {
                quality = qvalue;
                type = form == ResultType.ASK || form == ResultType.SELECT ? OutputType.SPARQL_XML : OutputType.RDF_XML;
            }
        }

        return type;
    }


    private static void writeSelectXml(HttpServletResponse res, Result result, boolean includeWarnings)
            throws IOException, SQLException
    {
        res.setHeader("content-type", "application/sparql-results+xml");
        res.setCharacterEncoding("UTF-8");

        PrintWriter out = res.getWriter();

        out.println("<?xml version=\"1.0\"?>");
        out.println("<sparql xmlns=\"http://www.w3.org/2005/sparql-results#\">");

        out.println("\t<head>");

        for(String head : result.getHeads())
        {
            out.print("\t\t<variable name=\"");
            writeXmlValue(out, head);
            out.println("\"/>");
        }

        out.println("\t</head>");

        out.println("\t<results>");

        while(result.next())
        {
            out.println("\t\t<result>");

            for(int i = 0; i < result.getHeads().size(); i++)
            {
                RdfNode node = result.get(i);

                if(node == null)
                    continue;


                out.print("\t\t\t<binding name=\"");
                writeXmlValue(out, result.getHeads().get(i));
                out.print("\">");

                if(node instanceof IriNode)
                {
                    out.print("<uri>");
                    writeXmlValue(out, node.getValue());
                    out.print("</uri>");
                }
                else if(node instanceof LanguageTaggedLiteral)
                {
                    out.print("<literal xml:lang=\"");
                    writeXmlValue(out, ((LanguageTaggedLiteral) node).getLanguage());
                    out.print("\">");
                    writeXmlValue(out, node.getValue());
                    out.print("</literal>");
                }
                else if(node instanceof TypedLiteral)
                {
                    out.print("<literal datatype=\"");
                    writeXmlValue(out, ((TypedLiteral) node).getDatatype().getValue());
                    out.print("\">");
                    writeXmlValue(out, node.getValue());
                    out.print("</literal>");
                }
                else if(node instanceof LiteralNode)
                {
                    out.print("<literal>");
                    writeXmlValue(out, node.getValue());
                    out.print("</literal>");
                }
                else if(node instanceof BNode)
                {
                    out.print("<bnode>");
                    writeXmlValue(out, node.getValue());
                    out.print("</bnode>");
                }

                out.println("</binding>");
            }

            out.println("\t\t</result>");
        }

        out.println("\t</results>");

        if(includeWarnings)
        {
            out.println("\t<warnings>");

            for(String warning : result.getWarnings())
            {
                out.print("\t\t<warning>");
                writeXmlValue(out, warning);
                out.println("</warning>");
            }

            out.println("\t</warnings>");
        }

        out.println("</sparql>");
    }


    private static void writeSelectJson(HttpServletResponse res, Result result, boolean includeWarnings)
            throws IOException, SQLException
    {
        res.setHeader("content-type", "application/sparql-results+json");
        res.setCharacterEncoding("UTF-8");

        PrintWriter out = res.getWriter();

        out.print("{\n\t\"head\": { \"vars\": [ ");

        boolean hasHead = false;

        for(String head : result.getHeads())
        {
            if(hasHead)
                out.print(", ");
            else
                hasHead = true;

            out.print('"');
            writeJsonValue(out, head);
            out.print('"');
        }

        out.println(" ]},\n\t\"results\": { \"bindings\": [");


        boolean hasResult = false;

        while(result.next())
        {
            if(hasResult)
                out.println(",");
            else
                hasResult = true;

            out.println("\t{");

            boolean hasResultHead = false;

            for(int i = 0; i < result.getHeads().size(); i++)
            {
                RdfNode node = result.get(i);

                if(node == null)
                    continue;


                if(hasResultHead)
                    out.println(',');
                else
                    hasResultHead = true;

                out.print("\t\t\"");
                writeJsonValue(out, result.getHeads().get(i));
                out.println("\": {");


                out.print("\t\t\t\"type\": ");

                if(node instanceof IriNode)
                    out.println("\"uri\",");
                else if(node instanceof LiteralNode)
                    out.println("\"literal\",");
                else
                    out.println("\"bnode\",");

                out.print("\t\t\t\"value\": ");

                out.print('"');
                writeJsonValue(out, node.getValue());
                out.print('"');

                if(node instanceof LanguageTaggedLiteral)
                {
                    out.print(",\n\t\t\t\"xml:lang\": ");

                    out.print('"');
                    writeJsonValue(out, ((LanguageTaggedLiteral) node).getLanguage());
                    out.print('"');
                }
                else if(node instanceof TypedLiteral)
                {
                    out.print(",\n\t\t\t\"datatype\": ");

                    out.print('"');
                    writeJsonValue(out, ((TypedLiteral) node).getDatatype().getValue());
                    out.print('"');
                }

                out.print("\n\t\t}");
            }

            out.print("\n\t}");
        }

        out.print("\n\t]}");

        if(includeWarnings)
        {
            out.print(",\n\t\"warnings\": { \"messages\": [\n");

            boolean hasWarning = false;

            for(String warning : result.getWarnings())
            {
                if(hasWarning)
                    out.print(",\n");
                else
                    hasWarning = true;

                out.print("\t\t\"");
                writeJsonValue(out, warning);
                out.print("\"");
            }

            out.print("\n\t]}");
        }

        out.print("\n}");
    }


    private static void writeSelectTsv(HttpServletResponse res, Result result) throws IOException, SQLException
    {
        res.setHeader("content-type", "text/tab-separated-values");
        res.setCharacterEncoding("UTF-8");

        PrintWriter out = res.getWriter();


        boolean hasHead = false;

        for(String head : result.getHeads())
        {
            if(hasHead)
                out.print("\t");
            else
                hasHead = true;

            writeTsvValue(out, head);
        }

        out.print("\r\n");


        while(result.next())
        {
            boolean hasResult = false;

            for(int i = 0; i < result.getHeads().size(); i++)
            {
                if(hasResult)
                    out.print('\t');
                else
                    hasResult = true;

                RdfNode node = result.get(i);

                if(node instanceof IriNode)
                {
                    out.print('<');
                    writeTsvIriValue(out, node.getValue());
                    out.print('>');
                }
                else if(node instanceof LanguageTaggedLiteral)
                {
                    out.print('"');
                    writeTsvLiteralValue(out, node.getValue());
                    out.print("\"@");
                    writeTsvValue(out, ((LanguageTaggedLiteral) node).getLanguage());
                }
                else if(node instanceof TypedLiteral)
                {
                    out.print('"');
                    writeTsvLiteralValue(out, node.getValue());
                    out.print("\"^^<");
                    writeTsvIriValue(out, ((TypedLiteral) node).getDatatype().getValue());
                    out.print('>');
                }
                else if(node instanceof LiteralNode)
                {
                    out.print('"');
                    writeTsvLiteralValue(out, node.getValue());
                    out.print('"');
                }
                else if(node instanceof BNode)
                {
                    out.print("_:");
                    writeTsvValue(out, node.getValue());
                }
            }

            out.print("\r\n");
        }
    }


    private static void writeSelectCsv(HttpServletResponse res, Result result) throws IOException, SQLException
    {
        res.setHeader("content-type", "text/csv");
        res.setCharacterEncoding("UTF-8");

        PrintWriter out = res.getWriter();


        boolean hasHead = false;

        for(String head : result.getHeads())
        {
            if(hasHead)
                out.print(",");
            else
                hasHead = true;

            writeCsvValue(out, head);
        }

        out.print("\r\n");


        while(result.next())
        {
            boolean hasResult = false;

            for(int i = 0; i < result.getHeads().size(); i++)
            {
                if(hasResult)
                    out.print(',');
                else
                    hasResult = true;

                RdfNode node = result.get(i);

                if(node != null)
                    writeCsvValue(out, node.getValue());
            }

            out.print("\r\n");
        }
    }


    private static void writeAskXml(HttpServletResponse res, Result result, boolean includeWarnings)
            throws IOException, SQLException
    {
        res.setHeader("content-type", "application/sparql-results+xml");
        res.setCharacterEncoding("UTF-8");

        PrintWriter out = res.getWriter();
        result.next();

        out.println("<?xml version=\"1.0\"?>");
        out.println("<sparql xmlns=\"http://www.w3.org/2005/sparql-results#\">");
        out.println("\t<head></head>");
        out.println("\t<boolean>" + result.get(0).getValue() + "</boolean>");
        out.println("</sparql>");
    }


    private static void writeAskJson(HttpServletResponse res, Result result, boolean includeWarnings)
            throws IOException, SQLException
    {
        res.setHeader("content-type", "application/sparql-results+json");
        res.setCharacterEncoding("UTF-8");

        PrintWriter out = res.getWriter();
        result.next();

        out.println("{");
        out.println("\t\"head\": { },");
        out.println("\t\"boolean\": " + result.get(0).getValue());
        out.println("}");
    }


    private static void writeAskTsv(HttpServletResponse res, Result result) throws IOException, SQLException
    {
        res.setHeader("content-type", "text/tab-separated-values");
        res.setCharacterEncoding("UTF-8");

        PrintWriter out = res.getWriter();
        result.next();

        out.println("\"bool\"");
        out.println(result.get(0).getValue());
    }


    private static void writeAskCsv(HttpServletResponse res, Result result) throws IOException, SQLException
    {
        res.setHeader("content-type", "text/csv");
        res.setCharacterEncoding("UTF-8");

        PrintWriter out = res.getWriter();
        result.next();

        out.println("\"bool\"");
        out.println(result.get(0).getValue());
    }


    private static void writeXmlValue(PrintWriter out, String value) throws IOException
    {
        for(char val : value.toCharArray())
        {
            if(val == '"')
                out.print("&quot;");
            else if(val == '\'')
                out.print("&apos;");
            else if(val == '<')
                out.print("&lt;");
            else if(val == '>')
                out.print("&gt;");
            else if(val == '&')
                out.print("&amp;");
            else
                out.print(val);
        }
    }


    private static void writeJsonValue(PrintWriter out, String value) throws IOException
    {
        for(char val : value.toCharArray())
        {
            if(val == '"')
                out.print("\\\"");
            else if(val == '\\')
                out.print("\\\\");
            else if(val == '\n')
                out.print("\\n");
            else if(val >= 32)
                out.print(val);
            else if(val < 10)
                out.print("\\u000" + (int) val);
            else if(val < 16)
                out.print("\\u000" + ('A' + val - 10));
            else if(val < 26)
                out.print("\\u001" + (val - 16));
            else if(val < 32)
                out.print("\\u001" + ('A' + val - 26));
        }
    }


    private static void writeTsvValue(PrintWriter out, String value) throws IOException
    {
        for(char val : value.toCharArray())
        {
            if(val == '\\')
                out.print("\\\\");
            else if(val == '\t')
                out.print("\\t");
            else if(val == '\r')
                out.print("\\r");
            else if(val == '\n')
                out.print("\\n");
            else
                out.print(val);
        }
    }


    private static void writeTsvIriValue(PrintWriter out, String value) throws IOException
    {
        for(char val : value.toCharArray())
        {
            if(val == '\\')
                out.print("\\\\");
            else if(val == '\t')
                out.print("\\t");
            else if(val == '\r')
                out.print("\\r");
            else if(val == '\n')
                out.print("\\n");
            else if(val == '>')
                out.print("\\>");
            else
                out.print(val);
        }
    }


    private static void writeTsvLiteralValue(PrintWriter out, String value) throws IOException
    {
        for(char val : value.toCharArray())
        {
            if(val == '\\')
                out.print("\\\\");
            else if(val == '\t')
                out.print("\\t");
            else if(val == '\r')
                out.print("\\r");
            else if(val == '\n')
                out.print("\\n");
            else if(val == '"')
                out.print("\\\"");
            else
                out.print(val);
        }
    }


    private static void writeCsvValue(PrintWriter out, String value) throws IOException
    {
        boolean mustBeQuoted = false;

        for(char val : value.toCharArray())
        {
            if(val == '"' || val == ',' || val == '\n' || val == '\r')
            {
                mustBeQuoted = true;
                break;
            }
        }


        if(mustBeQuoted)
            out.print('"');

        for(char val : value.toCharArray())
        {
            if(val == '"')
                out.print("\"\"");
            else
                out.print(val);
        }

        if(mustBeQuoted)
            out.print('"');
    }
}
