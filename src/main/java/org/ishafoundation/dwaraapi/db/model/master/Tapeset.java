package org.ishafoundation.dwaraapi.db.model.master;
		
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.model.master.jointables.LibraryclassTapeset;


@Entity
@Table(name="tapeset")
public class Tapeset {

	@Id
	@Column(name="id")
	private int id;
	
	@Column(name="name")
	private String name;

	@Column(name="barcode_prefix")
	private String barcodePrefix;

	// unidirectional reference is enough. .
	@OneToOne
	//@J  --oinColumn(name="storageformat_id")
	private Storageformat storageformat;
	
    @OneToMany(mappedBy = "tapeset",
            cascade = CascadeType.MERGE,
            orphanRemoval = true)
    List<LibraryclassTapeset> libraryclassTapeset = new ArrayList<>();
	
	public Tapeset() {}
	
	public Tapeset(int id, String name, String barcodePrefix) {
		this.id = id;
		this.name = name;
		this.barcodePrefix = barcodePrefix;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBarcodePrefix() {
		return barcodePrefix;
	}

	public void setBarcodePrefix(String barcodePrefix) {
		this.barcodePrefix = barcodePrefix;
	}

	public Storageformat getStorageformat() {
		return storageformat;
	}

	public void setStorageformat(Storageformat storageformat) {
		this.storageformat = storageformat;
	}

	public List<LibraryclassTapeset> getLibraryclassTapeset() {
		return libraryclassTapeset;
	}

	public void setLibraryclassTapeset(List<LibraryclassTapeset> libraryclassTapeset) {
		this.libraryclassTapeset = libraryclassTapeset;
	}
	
	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tapeset tapeset = (Tapeset) o;
        return Objects.equals(name, tapeset.name);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
	
}