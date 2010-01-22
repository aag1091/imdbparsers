package imdb.parsers.listtoxml.test;




public class Movies {

    public static void main(String[] args){
	System.out.println("test remove year");
	String test1 = "Bröderna Östermans huskors (1932)";
	String test2 = "\"3rd Rock from the Sun\" (1996) {Fear and Loathing in Rutherford (#6.2)}";
	String test3 = "Me and Gott (1918/I)";
	String test4 = "Me and Gott (1918/II)";
	String test5 = "Me and Gott (1918/III)";
	String test6 = "Me and Gott (191/I)";
	String test7 = "Me and Gott (191/II)";
	String test8 = "Me and Gott (191)";
	testYearRegex(test1, "Bröderna Östermans huskors ");
	testYearRegex(test2, "\"3rd Rock from the Sun\"  {Fear and Loathing in Rutherford (#6.2)}");
	testYearRegex(test3, "Me and Gott ");
	testYearRegex(test4, "Me and Gott ");
	testYearRegex(test5, "Me and Gott ");
	testYearRegex(test6, "Me and Gott (191/I)");
	testYearRegex(test7, "Me and Gott (191/II)");
	testYearRegex(test8, "Me and Gott (191)");
	
	System.out.println("test remove outside double quotes");
	String test9 = "\"X-Files\"";
	String test10 = "\"X-Fi\"es\"";
	String test11 = "X \"Files\"";
	String test12 = "X 'Files'";
	String test13 = "\"X 'Files'\"";
	String test14 = "X-Files";
	testRemoveOutsideQuotes(test9, "X-Files");
	testRemoveOutsideQuotes(test10, "X-Fi\"es");
	testRemoveOutsideQuotes(test11, "X \"Files\"");
	testRemoveOutsideQuotes(test12, "X 'Files'");
	testRemoveOutsideQuotes(test13, "X 'Files'");
	testRemoveOutsideQuotes(test14, "X-Files");
	
	System.out.println("test get movie name only");
	testGetMovieNameOnly(test1, "Bröderna Östermans huskors");
	testGetMovieNameOnly(test2, "3rd Rock from the Sun");
	testGetMovieNameOnly(test3, "Me and Gott");
	
	System.out.println("test movie identification");
	String test15 = "unspecified (1999)";
	String test16 = "video game (1999) (VG)";
	String test17 = "for-video-movie (1999) (V)";
	String test18 = "\"tv show\" (1999) (TV) {episode 1}";
	String test19 = "\"tv show no tv tag\" (1999) {episode 1}";
	testIsMovie(test15, true);
	testIsMovie(test16, false);
	testIsMovie(test17, true);
	testIsMovie(test18, false);
	testIsTVShow(test15, false);
	testIsTVShow(test16, false);
	testIsTVShow(test17, false);
	testIsTVShow(test18, true);
	testIsUseful(test15, true);
	testIsUseful(test16, false);
	testIsUseful(test17, true);
	testIsUseful(test18, false);
	testIsUseful(test19, false);
	
	System.out.println("test movie name uniqueness");
	testIsUniqueNameForYear(test3, false);
	testIsUniqueNameForYear(test4, false);
	testIsUniqueNameForYear(test5, false);
	testIsUniqueNameForYear(test6, true);
	testIsUniqueNameForYear(test7, true);
	testIsUniqueNameForYear(test8, true);
	testIsUniqueNameForYear(test1, true);
	
	System.out.println("get movie non-unique name identifier");
	testGetNameIdentifier(test3, "I");
	testGetNameIdentifier(test4, "II");
	testGetNameIdentifier(test5, "III");
	testGetNameIdentifier(test6, true);
	testGetNameIdentifier(test7, true);
	testGetNameIdentifier(test8, true);
	testGetNameIdentifier(test1, true);
	testGetNameIdentifier(test2, true);
	
	System.out.println("get year from name");
	testGetNameFromYear(test1, "1932");
	testGetNameFromYear(test2, "1996");
	testGetNameFromYear(test3, "1918");
	testGetNameFromYear(test4, "1918");
	testGetNameFromYear(test6, true);
	testGetNameFromYear(test7, true);
	testGetNameFromYear(test8, true);
	
	System.out.println("name has year");
	testNameHasYear(test1, true);
	testNameHasYear(test3, true);
	testNameHasYear(test6, false);
    }
    

    /**
     * the REGEX_YEAR matches the year part of a name.
     */
    private static void testYearRegex(String fullName, String need){
	printResult(fullName, need, fullName.replaceAll(imdb.parsers.listtoxml.Movies.REGEX_YEAR, ""));
    }
    
    private static void testRemoveOutsideQuotes(String fullName, String need){
	printResult(fullName, need, imdb.parsers.listtoxml.Movies.removeOutsideQuotes(fullName));
    }
    
    /**
     * Movies.getMovieNameOnly must return the actual name, without quotes (added to tv shows), and without year or any other tags. 
     */
    private static void testGetMovieNameOnly(String fullName, String need){
	printResult(fullName, need, imdb.parsers.listtoxml.Movies.getMovieNameOnly(fullName));
    }
    
    private static void testIsMovie(String fullName, boolean need){
	printResult(fullName, need, imdb.parsers.listtoxml.Movies.isMovie(fullName));
    }
    private static void testIsTVShow(String fullName, boolean need){
	printResult(fullName, need, imdb.parsers.listtoxml.Movies.isTVShow(fullName));
    }
    private static void testIsUseful(String fullName, boolean need){
	printResult(fullName, need, imdb.parsers.listtoxml.Movies.isUseful(fullName));
    }
    
    /**
     * Tests identifying whether or not a name-year pair is unique.
     * e.g. Cats (2001) is unique, Dogs(2005/II) is not, there is at least one other title named Dogs released in 2005.
     */
    private static void testIsUniqueNameForYear(String fullName, boolean need){
	printResult(fullName, need, imdb.parsers.listtoxml.Movies.isUniqueNameForYear(fullName));
    }
    
    private static void testGetNameIdentifier(String fullName, String need){
	printResult(fullName, need, imdb.parsers.listtoxml.Movies.getInYearIdentifier(fullName));
    }
    
    private static void testGetNameIdentifier(String fullName, boolean exception){
	boolean threw;
	String testReturn = null;
	try{
	    testReturn = imdb.parsers.listtoxml.Movies.getInYearIdentifier(fullName);
	    threw = false;
	}catch(IllegalArgumentException e){
	    threw = true;
	}
	printResult(fullName, "exception", threw? "exception" : testReturn);
    }
    
    /**
     * get year from name
     */
    private static void testGetNameFromYear(String fullName, String need){
	printResult(fullName, need, imdb.parsers.listtoxml.Movies.getYearFromName(fullName));
    }
    
    private static void testGetNameFromYear(String fullName, boolean exception){
	boolean threw;
	String testReturn = null;
	try{
	    testReturn = imdb.parsers.listtoxml.Movies.getYearFromName(fullName);
	    threw = false;
	}catch(IllegalArgumentException e){
	    threw = true;
	}
	printResult(fullName, "exception", threw? "exception" : testReturn);
    }
    
    private static void testNameHasYear(String fullName, boolean need){
	printResult(fullName, need, imdb.parsers.listtoxml.Movies.hasYearInName(fullName));
    }
    
    /**
     * Print methods
     */
    private static void printResult(String fullName, String need, String testReturn){
	if(need.equals(testReturn)){
	    System.out.println("\tPASS - Need: "+need+", Got: "+testReturn+", with: "+fullName);
	}else{
	    System.out.println("***\tFAIL - Need: "+need+", Got: "+testReturn+", with: "+fullName);
	}
    }
    
    private static void printResult(String fullName, boolean need, boolean testReturn){
	if(need == testReturn){
	    System.out.println("\tPASS - Need: "+need+", Got: "+testReturn+", with: "+fullName);
	}else{
	    System.out.println("***\tFAIL - Need: "+need+", Got: "+testReturn+", with: "+fullName);
	}
    }
}
