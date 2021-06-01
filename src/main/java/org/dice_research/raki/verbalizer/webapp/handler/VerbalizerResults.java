package org.dice_research.raki.verbalizer.webapp.handler;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class VerbalizerResults {

  private static final Logger LOG = LogManager.getLogger(VerbalizerResults.class);

  public Set<Map<String, Object>> response = new HashSet<>();

  public void setResponse(final JSONArray ja) {
    response = new HashSet<>();
    if (ja != null && !ja.isEmpty()) {
      for (int i = 0; i < ja.length(); i++) {
        response.add(ja.getJSONObject(i).toMap());
      }
    }
  }

  @Override
  public String toString() {
    final ObjectMapper objectMapper = new ObjectMapper();
    String s = "";
    try {
      s = objectMapper.writeValueAsString(this);
    } catch (final JsonProcessingException e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
    return s;
  }
}
