package cz.iocb.chemweb.server.sparql.mapping;

import java.util.List;
import cz.iocb.chemweb.server.db.DatabaseSchema.KeyPair;
import cz.iocb.chemweb.server.sparql.mapping.classes.ResourceClass;



public interface ParametrisedMapping
{
    ResourceClass getResourceClass();

    String getSqlValueAccess(int part);

    String getSqlColumn(int part);

    NodeMapping remapColumns(List<KeyPair> columnMap);
}
