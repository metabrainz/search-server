package org.musicbrainz.search.servlet;

import java.util.HashMap;
import java.util.Map;

import org.musicbrainz.search.index.AnnotationIndexField;

public class AnnotationDismaxSearch extends AbstractDismaxSearchServer {

  @Override
  protected DismaxSearcher initDismaxSearcher() {
    Map<String, DismaxAlias.AliasField> fieldBoosts = new HashMap<String, DismaxAlias.AliasField>(2);
    fieldBoosts.put(AnnotationIndexField.NAME.getName(), new DismaxAlias.AliasField(true, 1f));
    fieldBoosts.put(AnnotationIndexField.TEXT.getName(), new DismaxAlias.AliasField(true, 1f));
    DismaxAlias dismaxAlias = new DismaxAlias();
    dismaxAlias.setFields(fieldBoosts);
    dismaxAlias.setTie(0.1f);
    return new DismaxSearcher(dismaxAlias);
  }

  /**
   * Standard Search
   *
   * @param searcher
   * @throws Exception
   */
  public AnnotationDismaxSearch(AbstractSearchServer searchServer) throws Exception {
    super(searchServer);
  }

}
