package com.infotel.mantis_api.service;

import com.infotel.mantis_api.exception.AccessDenied;
import com.infotel.mantis_api.model.Relation;
import com.infotel.mantis_api.model.Relations;

import java.util.List;

public interface RelationsService {
    // Get Relations
    List<Relation> searchRelations(int id);
    
    // Add Relations
    String addRelations(int id, Relations relations) throws AccessDenied;
    
    // Delete Relations
    String deleteRelations(int id, Relations relations);
}
