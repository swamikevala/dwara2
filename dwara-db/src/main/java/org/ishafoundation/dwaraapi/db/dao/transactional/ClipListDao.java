package org.ishafoundation.dwaraapi.db.dao.transactional;

import org.ishafoundation.dwaraapi.db.model.transactional.ClipList;
import org.ishafoundation.dwaraapi.db.model.transactional.ClipTag;
import org.springframework.data.repository.CrudRepository;

public interface ClipListDao extends CrudRepository<ClipList,Integer> {
    public ClipList findByName(String name);
    public void deleteById(int id);
}
