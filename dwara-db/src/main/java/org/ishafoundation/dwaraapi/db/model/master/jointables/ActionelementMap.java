package org.ishafoundation.dwaraapi.db.model.master.jointables;
		
import java.util.Objects;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.keys.ActionelementMapKey;

@Entity(name = "ActionelementMap")
@Table(name="actionelement_map")
public class ActionelementMap {

	@EmbeddedId
	private ActionelementMapKey id;

	public ActionelementMap() {
		
	}

	public ActionelementMap(Actionelement actionelement, Actionelement actionelementRef) {
		this.id = new ActionelementMapKey(actionelement.getId(), actionelementRef.getId());
	}
	
	public ActionelementMapKey getId() {
		return id;
	}

	public void setId(ActionelementMapKey id) {
		this.id = id;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
 
        if (o == null || getClass() != o.getClass())
            return false;
 
        ActionelementMap that = (ActionelementMap) o;
        return Objects.equals(id, that.id);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}