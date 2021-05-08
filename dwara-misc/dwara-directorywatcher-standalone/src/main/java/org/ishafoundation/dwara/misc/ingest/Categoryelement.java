package org.ishafoundation.dwara.misc.ingest;

public enum Categoryelement {

	
	A(1, "A - Inner Engineering", "A", "Private1"),
	B(2, "B - Isha Yoga Programs", "B", "Private1"),
	C(3, "C - Special Programs", "C", "Public"),
	D(4, "D - Samyama", "D", "Private1"),
	E(5, "E - Executive Wholeness", "E", "Private1"),
	F(6, "F - BSP Programs", "F", "Private1"),
	G(7, "G - Dhyan Yatra_Trekking", "G", "Public"),
	H(8, "H - Mahashivarathri", "H", "Public"),
	I(9, "I - Isha Fest", "I", "Public"),
	J(10, "J - Mahasathsangs", "J", "Public"),
	K(11, "K - Special Events", "K", "Public"),
	L(12, "L - Conferences", "L", "Public"),
	M(13, "M-Meditator Volunteer Sathsangs", "M", "Private1"),
	N(14, "N - Introductory Talks", "N", "Public"),
	P(15, "P - Resident Sathsangs", "P", "Private1"),
	Q(16, "Q - Interviews_Press_Recordings", "Q", "Private1"),
	R(17, "R- Sadhguru's Visits & Activity", "R", "Private1"),
	S(18, "S - Dhyanalinga", "S", "Public"),
	T(19, "T - Ashram Events", "T", "Public"),
	U(20, "U - Prison Program Material", "U", "Public"),
	V(21, "V - Construction", "V", "Public"),
	W(22, "W - Teachers Training", "W", "Private2"),
	X(23, "X - Private Meetings", "X", "X-Public/Private"),
	Z(24, "Z - Edited Material", "Z", "Z-Public/Private"),
	AA(25, "AA - Vanashree Eco Center", "AA", "Public"),
	AB(26, "AB - Sharing", "AB", "Public"),
	AC(27, "AC - ARR", "AC", "Public"),
	AD(28, "AD - Committee Meets", "AD", "Private1"),
	AE(29, "AE - Tsunami Relief", "AE", "Public"),
	AF(30, "AF - ARR Function", "AF", "Public"),
	AG(31, "AG - Isha Yoga Practices_Class", "AG", "Private1"),
	AH(32, "AH - ISHA HOME SCHOOL", "AH", "Private1"),
	AI(33, "AI - Isha Rejuvenation", "AI", "Public"),
	AJ(34, "AJ - Sounds Of Isha", "AJ", "Public"),
	AK(35, "AK - Isha Samskriti", "AK", "Private1"),
	AL(36, "AL - Ashram Life", "AL", "Public"),
	AM(37, "AM - Isha Initiative & Movem", "AM", "Public"),
	AN(38, "AN-Hata Yoga Teacher's Training", "AN", "Private1"),
	AR(39, "AR-Isha Arogya", "AR", "Public"),
	AS(40, "AS - Isha Vidya", "AS", "Public"),
	AW(41, "AW - Special Workshops", "AW", "Public"),
	AX(42, "AX - Sacred Spaces", "AX", "Public"),
	AY(43, "AY - Project Green Hands", "AY", "Public"),
	AZ(44, "AZ-US CENTRE", "AZ", "Private1"),
	BB(45, "BB - Gramotsav", "BB", "Public"),
	BC(46, "BC - External Shoots", "BC", "Public"),
	BD(47, "BD - Sadhguru's Class Recording", "BD", "Private1"),
	BE(48, "BE - Non - Isha Material", "BE", "Public"),
	BG(49, "BG-Sadhguru Darshan", "BG", "Public"),
	BJ(50, "BJ - Radhe", "BJ", "Public"),
	TV(51, "TV - Television Programs", "TV", "Public"),
	Y(52, "Y - Brahmacharies", "Y", "Private2"),
	ZY(53, "ZY - Edited Private2", "ZY", "Z-Private2"),
	ZP(54, "ZP - Edited Private1", "ZP", "Z-Private1"),
	GR(55, "GR - ???", "GR", "GR-Public/Private");
	
	private int sNo;
	private String categoryName;
	private String categoryNamePrefix;
	private String category;

	Categoryelement(int sNo, String categoryName, String categoryNamePrefix, String category){
		this.sNo = sNo;
		this.categoryName = categoryName;
		this.categoryNamePrefix = categoryNamePrefix;
		this.category = category;
	}
	
	public int getsNo() {
		return sNo;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public String getCategoryNamePrefix() {
		return categoryNamePrefix;
	}

	public String getCategory() {
		return category;
	}
	
//	public static List<Categoryelement> findAllByCategory(String categoryType){
//		List<Categoryelement> categoryList = new ArrayList<Categoryelement>();
//	    for (Categoryelement nthCategory : Categoryelement.values()) {
//	        if (nthCategory.getCategory().equals(categoryType)) {
//	        	categoryList.add(nthCategory);
//	        }
//	    }
//		return categoryList;
//	}

}
