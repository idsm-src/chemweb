package cz.iocb.chemweb.server.sparql.parser.visitor;

import java.util.List;
import java.util.stream.Collectors;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;
import cz.iocb.chemweb.server.sparql.grammar.SparqlParser.IriContext;
import cz.iocb.chemweb.server.sparql.grammar.SparqlParser.PathAlternativeContext;
import cz.iocb.chemweb.server.sparql.grammar.SparqlParser.PathEltContext;
import cz.iocb.chemweb.server.sparql.grammar.SparqlParser.PathEltOrInverseContext;
import cz.iocb.chemweb.server.sparql.grammar.SparqlParser.PathNegatedPropertySetContext;
import cz.iocb.chemweb.server.sparql.grammar.SparqlParser.PathOneInPropertySetContext;
import cz.iocb.chemweb.server.sparql.grammar.SparqlParser.PathPrimaryContext;
import cz.iocb.chemweb.server.sparql.grammar.SparqlParser.PathSequenceContext;
import cz.iocb.chemweb.server.sparql.parser.Rdf;
import cz.iocb.chemweb.server.sparql.parser.model.IRI;
import cz.iocb.chemweb.server.sparql.parser.model.triple.AlternativePath;
import cz.iocb.chemweb.server.sparql.parser.model.triple.BracketedPath;
import cz.iocb.chemweb.server.sparql.parser.model.triple.InversePath;
import cz.iocb.chemweb.server.sparql.parser.model.triple.NegatedPath;
import cz.iocb.chemweb.server.sparql.parser.model.triple.Path;
import cz.iocb.chemweb.server.sparql.parser.model.triple.RepeatedPath;
import cz.iocb.chemweb.server.sparql.parser.model.triple.SequencePath;



public class PathVisitor extends BaseVisitor<Path>
{
    private final QueryVisitorContext context;

    public PathVisitor(QueryVisitorContext context)
    {
        this.context = context;
    }

    private List<Path> visitPathList(List<? extends ParserRuleContext> pathSequenceContexts)
    {
        return pathSequenceContexts.stream().map(this::visit).collect(Collectors.toList());
    }

    @Override
    public Path visitPathAlternative(PathAlternativeContext ctx)
    {
        if(ctx.pathSequence().size() == 1)
            return visit(ctx.pathSequence(0));

        List<Path> paths = visitPathList(ctx.pathSequence());

        return new AlternativePath(paths);
    }

    @Override
    public Path visitPathSequence(PathSequenceContext ctx)
    {
        if(ctx.pathEltOrInverse().size() == 1)
            return visit(ctx.pathEltOrInverse(0));

        List<Path> paths = visitPathList(ctx.pathEltOrInverse());

        return new SequencePath(paths);
    }

    @Override
    public Path visitPathEltOrInverse(PathEltOrInverseContext ctx)
    {
        Path path = visit(ctx.pathElt());

        if(ctx.INVERSE() == null)
            return path;

        return new InversePath(path);
    }

    @Override
    public Path visitPathElt(PathEltContext ctx)
    {
        Path path = visit(ctx.pathPrimary());

        if(ctx.pathMod() == null)
            return path;

        RepeatedPath.Kind repeatKind = null;

        switch(ctx.pathMod().getText())
        {
            case "?":
                repeatKind = RepeatedPath.Kind.ZeroOrOne;
                break;
            case "*":
                repeatKind = RepeatedPath.Kind.ZeroOrMore;
                break;
            case "+":
                repeatKind = RepeatedPath.Kind.OneOrMore;
                break;
        }

        return new RepeatedPath(repeatKind, path);
    }

    @Override
    public Path visitPathPrimary(PathPrimaryContext ctx)
    {
        if(ctx.iri() != null)
        {
            return visit(ctx.iri());
        }

        if(ctx.A() != null)
        {
            return new IRI(Rdf.TYPE);
        }

        if(ctx.pathNegatedPropertySet() != null)
        {
            return new NegatedPath(visit(ctx.pathNegatedPropertySet()));
        }

        return new BracketedPath(visit(ctx.path()));
    }

    @Override
    public IRI visitIri(IriContext ctx)
    {
        return new IriVisitor(context).visit(ctx);
    }

    @Override
    public Path visitPathNegatedPropertySet(PathNegatedPropertySetContext ctx)
    {
        if(ctx.pathOneInPropertySet().size() == 1)
        {
            Path result = visit(ctx.pathOneInPropertySet(0));

            // I don't think there is a better way to check for existence of the
            // parenthesis
            // without making the grammar more complicated
            if(ctx.getChild(0) instanceof TerminalNode)
                result = new BracketedPath(result);

            return result;
        }

        return new BracketedPath(new AlternativePath(visitPathList(ctx.pathOneInPropertySet())));
    }

    @Override
    public Path visitPathOneInPropertySet(PathOneInPropertySetContext ctx)
    {
        Path path;

        if(ctx.iri() != null)
        {
            path = visit(ctx.iri());
        }
        else
        {
            path = new IRI(Rdf.TYPE);
        }

        if(ctx.INVERSE() == null)
        {
            return path;
        }

        return new InversePath(path);
    }
}
