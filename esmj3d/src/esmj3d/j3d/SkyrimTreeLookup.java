package esmj3d.j3d;

import java.util.HashMap;

public class SkyrimTreeLookup
{
	// so check to make sure skyrim data exists some how
	// then look up here

	private static HashMap<String, String> map = null;

	private static void init()
	{
		map = new HashMap<String, String>();
		for (int i = 0; i < treeMap.length; i++)
		{
			map.put(treeMap[i][0], treeMap[i][1]);
		}
	}

	public static String getLookup(String sptFileName)
	{
		//TODO: do we have skyrim data in a bsa file? if not no mapping thanks
		if (map == null)
			init();

		String key = sptFileName;
		// chop ending spt
		if (key.endsWith(".spt"))
			key = sptFileName.substring(0, key.length() - ".spt".length());

		//chop starting \
		if (key.startsWith("\\"))
			key = key.substring("\\".length());

		// lowercase it too
		key = key.toLowerCase();

		String newTreeName = map.get(key);
		
		if (newTreeName == null)
		{
			// this is fine for now, assume shrubs or something
			//System.err.println("sptFileName tree not mapped! " + sptFileName);
			return null;
		}
		else
		{
			return "landscape\\" + newTreeName + ".nif";
		}

	}

	private static String[][] treeMap = new String[][]
	{

		// shrubs set back to X's
		
	/*{ "shrubvinemaplesu", "plants\\vinemaple01" }, //
			{ "shrubazaleapinksu", "plants\\fallforestshrub01" }, //
			{ "shrubazaleasu", "plants\\fallforestshrub02" }, //
			{ "shrubboxwood", "plants\\fallforestferncluster01" }, //
			{ "shrubboxwoodfa", "plants\\fallforestferncluster02" }, //
			{ "shrubcamoranparadise01", "plants\\deadshrub01" }, //
			{ "shrubdaphnesu", "plants\\fallforestferncluster03" }, //
			{ "shrubdeadbush", "plants\\deadshrub01" }, //
			{ "shrubelderberry", "plants\\reachbush01" }, //
			{ "shrubenglishholly", "plants\\deadshrub01" }, //
			{ "shrubenglishhollysnow", "plants\\deadshrub01" }, //
			{ "shrubeuonymussnow", "plants\\deadshrub01" }, //
			{ "shrubeuonymussu", "plants\\deadshrub01" }, //
			{ "shrubforsythiasu", "plants\\deadshrub01" }, //
			{ "shrubgenericbuckthornfa", "plants\\deadshrub01" }, //
			{ "shrubgenericbuckthornsu", "plants\\deadshrub01" }, //
			{ "shrubgenericdaphnefa", "plants\\deadshrub01" }, //
			{ "shrubgenericdaphnesu", "plants\\deadshrub01" }, //
			{ "shrubgenericelderberryfa", "plants\\deadshrub01" }, //
			{ "shrubgenericelderberrysu", "plants\\deadshrub01" }, //
			{ "shrubgenericinkberryfa", "plants\\deadshrub01" }, //
			{ "shrubgenericinkberrysu", "plants\\deadshrub01" }, //
			{ "shrubhydrangeabluesu", "plants\\deadshrub01" }, //
			{ "shrubinkberrysu", "plants\\deadshrub01" }, //
			{ "shrubjaphoneysucklesu", "plants\\deadshrub01" }, //
			{ "shrubms14boxwood", "plants\\deadshrub01" }, //
			{ "shrubmugopine", "plants\\deadshrub01" }, //
			{ "shrubmugopinefa", "plants\\deadshrub01" }, //
			{ "shrubrhododendronsu", "plants\\deadshrub01" }, //
			{ "shrubseabuckthornsu", "plants\\deadshrub01" }, //
			{ "shrubvinemaplesnow", "plants\\vinemaple01snow" }, //
			{ "shrubvinemaplesu", "plants\\vinemaple01" }, //
			{ "shrubvinemaplewi", "plants\\deadshrub01" }, //*/
			{ "treeblacklocust", "trees\\reachtree01" }, //
			{ "treecamoranparadise01", "trees\\treeaspen01" }, //
			{ "treecamoranparadise02", "trees\\treeaspen01" }, //
			{ "treecamoranparadise04", "trees\\treeaspen01" }, //
			{ "treecottonwoodsu", "trees\\treeaspen01" }, //
			{ "treecpginkgo", "trees\\treeaspen02" }, //
			{ "treecpsnowgum", "trees\\treeaspen03" }, //
			{ "treecpswampcypressforest01", "trees\\treeaspen04" }, //
			{ "treedeodar", "trees\\treeaspen05" }, //
			{ "treedogwoodsu", "trees\\treeaspen06" }, //
			{ "treeeasthemlock", "trees\\treepineforest01" }, //
			{ "treeeasthemlockfa", "trees\\treepineforest01" }, //
			{ "treeeasthemlocksnow", "trees\\treepineforest01" }, //
			{ "treeenglishholly01", "trees\\treepineforest02" }, //
			{ "treeenglishoakforest01fa", "trees\\treepineforest03" }, //
			{ "treeenglishoakforest01su", "trees\\treepineforest03" }, //
			{ "treeenglishoakforestfa", "trees\\treepineforest03" }, //
			{ "treeenglishoakforestsu", "trees\\treepineforest03" }, //
			{ "treeenglishoakfreesu", "trees\\treepineforest03" }, //
			{ "treeenglishoaksaplingfa", "trees\\reachtree01" }, //
			{ "treeenglishoaksaplingsu", "trees\\reachtree01" }, //
			{ "treeenglishoakunique01su", "trees\\reachtree01" }, //
			{ "treeenglishoakyoungfa", "trees\\reachtree01" }, //
			{ "treeenglishoakyoungsu", "trees\\reachtree01" }, //
			{ "treegcsycamoresu", "trees\\reachtree01" }, //
			{ "treeginkgo", "trees\\reachtree01" }, //
			{ "treegreypoplarforestfa", "trees\\treepineforest04" }, //
			{ "treegreypoplarforestsu", "trees\\treepineforest04" }, //
			{ "treeironwoodsu", "trees\\treepineforest05" }, //
			{ "treeironwoodwi", "trees\\treepineforest05" }, //
			{ "treejapanesemaple", "trees\\treepineforest05" }, //
			{ "treejuniper01", "trees\\treepineforest05" }, //
			{ "treekvatchburnt", "trees\\treepineforest05" }, //
			{ "treems14canvasfree", "trees\\treepineforest05" }, //
			{ "treems14canvasfreesu", "trees\\treepineforest05" }, //
			{ "treems14willowoakforest01su", "trees\\treepineforest05" }, //
			{ "treems14willowoakforestsu", "trees\\treepineforest05" }, //
			{ "treems14willowoakfreesu", "trees\\treepineforest05" }, //
			{ "treems14willowoakyoungsu", "trees\\treepineforest05" }, //
			{ "treequakingaspenforest01fa", "trees\\treepineforest05" }, //
			{ "treequakingaspenforest01su", "trees\\treepineforest05" }, //
			{ "treequakingaspenforestfa", "trees\\treepineforest05" }, //
			{ "treequakingaspenforestsu", "trees\\treepineforest05" }, //
			{ "treequakingaspenfreesu", "trees\\treepineforest05" }, //
			{ "treequakingaspenyoungfa", "trees\\treepineforest05" }, //
			{ "treequakingaspenyoungsu", "trees\\treepineforest05" }, //
			{ "treeredwoodlarge", "trees\\treepineforest05" }, //
			{ "treeredwoodsmall", "trees\\treepineforest05" }, //
			{ "treescotchpineforest", "trees\\treepineforest05" }, //
			{ "treescotchpineforestsnow", "trees\\treepineforest05" }, //
			{ "treesilverbirchforest01fa", "trees\\treepineforest05" }, //
			{ "treesilverbirchforest01su", "trees\\treepineforest05" }, //
			{ "treesilverbirchforestfa", "trees\\treepineforest05" }, //
			{ "treesilverbirchforestsu", "trees\\treepineforest05" }, //
			{ "treesilverbirchfreesu", "trees\\treepineforest05" }, //
			{ "treesilverbirchyoungfa", "trees\\treepineforest05" }, //
			{ "treesilverbirchyoungsu", "trees\\treepineforest05" }, //
			{ "treesnowgumfree", "trees\\treepineforest05" }, //
			{ "treesugarmapleforest01fa", "trees\\treepineforest05" }, //
			{ "treesugarmapleforest01su", "trees\\treepineforest05" }, //
			{ "treesugarmapleforestfa", "trees\\treepineforest05" }, //
			{ "treesugarmapleforestsu", "trees\\treepineforest05" }, //
			{ "treesugarmaplefreesu", "trees\\treepineforest05" }, //
			{ "treesugarmaplesaplingfa", "trees\\treepineforest05" }, //
			{ "treesugarmaplesaplingsu", "trees\\treepineforest05" }, //
			{ "treesugarmapleyoungfa", "trees\\treepineforest05" }, //
			{ "treesugarmapleyoungsu", "trees\\treepineforest05" }, //
			{ "treeswampcypress", "trees\\treepineforest05" }, //
			{ "treeswampcypressforest", "trees\\treepineforest05" }, //
			{ "treeswampcypressforestfg08", "trees\\treepineforest05" }, //
			{ "treetupelo", "trees\\treepineforest05" }, //
			{ "treeweepingwillowsu", "trees\\treepineforest05" }, //
			{ "treewhitepineforest", "trees\\treepineforest05" }, //
			{ "treewhitepineforest01", "trees\\treepineforest05" }, //
			{ "treewhitepineforest01fa", "trees\\treepineforest05" }, //
			{ "treewhitepineforestfa", "trees\\treepineforest05" }, //
			{ "treewhitepinefree", "trees\\treepineforest05" }, //
			{ "treewhitepinesnow", "trees\\treepineforest05" }, //
			{ "treewhitepineyoung", "trees\\treepineforest05" }, //
			{ "treewhitepineyoungfa", "trees\\treepineforest05" }, //
			{ "treewillowoakforest01su", "trees\\treepineforest05" }, //
			{ "treewillowoakforestsu", "trees\\treepineforest05" }, //
			{ "treewillowoakfreesu", "trees\\treepineforest05" }, //
			{ "treewillowoakyoungsu", "trees\\treepineforest05" }, //
			{ "treeyewforest", "trees\\treepineforest05" }, //
			//////////////////////////////
			
			 
			// no fallout
			/*{ "euonymusbush01", "trees\\reachtree01" }, //
			{ "oasiselm01", "trees\\reachtree01" }, //
			{ "oasiselm02", "trees\\reachtree01" }, //
			{ "oasistreetop01", "trees\\reachtree01" }, //
			{ "pine01", "trees\\reachtree01" }, //
			{ "sugarmaple01", "trees\\reachtree01" }, //
			{ "sycamore01", "trees\\reachtree01" }, //
			{ "wastelandshrub01", "trees\\reachtree01" }, //
			{ "wastelandundergrowth01", "trees\\reachtree01" }, //
			{ "whiteoak01", "trees\\reachtree01" }, //*/

	};

 

}
