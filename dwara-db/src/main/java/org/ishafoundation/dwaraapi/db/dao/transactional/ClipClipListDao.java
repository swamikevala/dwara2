package org.ishafoundation.dwaraapi.db.dao.transactional;

import org.ishafoundation.dwaraapi.db.model.transactional.ClipClipList;
import org.ishafoundation.dwaraapi.db.model.transactional.ClipTag;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ClipClipListDao extends CrudRepository<ClipClipList,Integer> {
    List<ClipClipList> findAllByCliplistId(int clipListId);
    void deleteByCliplistId(int clipListId);

    ClipClipList findByCliplistId(int clipListId);

    ClipClipList findByCliplistIdAndClipId(int clipListId, int clipId);
}
