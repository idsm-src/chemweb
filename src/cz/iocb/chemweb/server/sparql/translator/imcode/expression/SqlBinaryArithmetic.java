package cz.iocb.chemweb.server.sparql.translator.imcode.expression;

import static cz.iocb.chemweb.server.sparql.mapping.classes.BuiltinClasses.xsdDecimal;
import static cz.iocb.chemweb.server.sparql.mapping.classes.BuiltinClasses.xsdDouble;
import static cz.iocb.chemweb.server.sparql.mapping.classes.BuiltinClasses.xsdFloat;
import static cz.iocb.chemweb.server.sparql.mapping.classes.BuiltinClasses.xsdInteger;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;
import cz.iocb.chemweb.server.sparql.mapping.classes.ResourceClass;
import cz.iocb.chemweb.server.sparql.parser.model.expression.BinaryExpression.Operator;
import cz.iocb.chemweb.server.sparql.parser.model.expression.Literal;
import cz.iocb.chemweb.server.sparql.translator.expression.VariableAccessor;



public class SqlBinaryArithmetic extends SqlBinary
{
    private final Operator operator;


    public SqlBinaryArithmetic(Operator operator, SqlExpressionIntercode left, SqlExpressionIntercode right,
            Set<ResourceClass> resourceClasses, boolean canBeNull)
    {
        super(left, right, resourceClasses, resourceClasses.size() > 1, canBeNull);
        this.operator = operator;
    }


    public static SqlExpressionIntercode create(Operator operator, SqlExpressionIntercode left,
            SqlExpressionIntercode right)
    {
        Set<ResourceClass> resultClasses = new HashSet<ResourceClass>();

        for(ResourceClass leftClass : left.getResourceClasses())
            for(ResourceClass rightClass : right.getResourceClasses())
                if(isNumeric(leftClass) && isNumeric(rightClass))
                    resultClasses.add(determineResultClass(operator, leftClass, rightClass));

        if(resultClasses.isEmpty())
            return SqlNull.get();

        return new SqlBinaryArithmetic(operator, left, right, resultClasses, left.canBeNull() || right.canBeNull()
                || left.getResourceClasses().stream().anyMatch(r -> !isNumeric(r))
                || right.getResourceClasses().stream().anyMatch(r -> !isNumeric(r))
                || operator == Operator.Divide && resultClasses.contains(xsdDecimal) && canBeDecimalZero(right));
    }


    private static boolean canBeDecimalZero(SqlExpressionIntercode operand)
    {
        if(!(operand instanceof SqlLiteral))
            return true;

        Literal literal = ((SqlLiteral) operand).getLiteral();
        Object value = literal.getValue();

        if(value instanceof Short)
            return (Short) value == 0;
        else if(value instanceof Integer)
            return (Integer) value == 0;
        else if(value instanceof Long)
            return (Long) value == 0l;
        else if(value instanceof BigInteger)
            return ((BigInteger) value).equals(BigInteger.ZERO);
        else if(value instanceof BigDecimal)
            return ((BigDecimal) value).equals(BigDecimal.ZERO);

        return false;
    }


    private static ResourceClass determineResultClass(Operator operator, ResourceClass leftClass,
            ResourceClass rightClass)
    {
        if(leftClass == xsdDouble || rightClass == xsdDouble)
            return xsdDouble;
        else if(leftClass == xsdFloat || rightClass == xsdFloat)
            return xsdFloat;
        else if(leftClass == xsdDecimal || rightClass == xsdDecimal || operator == Operator.Divide)
            return xsdDecimal;
        else
            return xsdInteger;
    }


    @Override
    public SqlExpressionIntercode optimize(VariableAccessor variableAccessor)
    {
        SqlExpressionIntercode left = getLeft().optimize(variableAccessor);
        SqlExpressionIntercode right = getRight().optimize(variableAccessor);
        return create(operator, left, right);
    }


    @Override
    public String translate()
    {
        String leftCode = translateAsNumeric(getLeft(), getExpressionResourceClass());
        String rightCode = translateAsNumeric(getRight(), getExpressionResourceClass());

        return "sparql." + operator.getName() + "_" + getResourceName() + "(" + leftCode + ", " + rightCode + ")";
    }
}
