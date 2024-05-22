package org.apache.geode.geospatial.domain;

import java.util.Collection;
import java.util.Collections;

import org.apache.geode.pdx.PdxInstance;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;

import org.apache.geode.cache.lucene.LuceneIndex;
import org.apache.geode.cache.lucene.LuceneSerializer;

public class LocationInfoSerializer implements LuceneSerializer<LocationEvent> {

    @Override

    public Collection<Document> toDocuments(LuceneIndex index, LocationEvent value) {

        Document doc = new Document();
        doc.add(new TextField("uid", value.getUid(), Field.Store.NO));

        Field[] fields = SpaitalHelper.getIndexableFields(value.getLng(), value.getLat());

        for (Field field : fields) {
            doc.add(field);
        }

        return Collections.singleton(doc);
    }

}

