package esmj3d.j3d.trees;

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
		
	{ "shrubvinemaplesu", "plants\\vinemaple01" }, //
			{ "shrubazaleapinksu", "plants\\fallforestshrub01" }, //
			{ "shrubazaleasu", "plants\\fallforestshrub02" }, //
			{ "shrubboxwood", "plants\\thicket01" }, //
			{ "shrubboxwoodfa", "plants\\thicket01" }, //
			{ "shrubcamoranparadise01", "plants\\swordfern01" }, //
			{ "shrubdaphnesu", "plants\\fallforestferncluster03" }, //
			{ "shrubdeadbush", "plants\\deadshrub01" }, //
			{ "shrubelderberry", "plants\\reachbush01" }, //
			{ "shrubenglishholly", "plants\\pineshrub01" }, //
			{ "shrubenglishhollysnow", "plants\\pineshrub01snow" }, //
			{ "shrubeuonymussnow", "plants\\deadshrub01snow" }, //
			{ "shrubeuonymussu", "plants\\deadshrub01" }, //
			{ "shrubforsythiasu", "plants\\deadshrub01" }, //
			{ "shrubgenericbuckthornfa", "plants\\yellowshrub01" }, //
			{ "shrubgenericbuckthornsu", "plants\\yellowshrub01" }, //
			{ "shrubgenericdaphnefa", "plants\\yellowshrub02" }, //
			{ "shrubgenericdaphnesu", "plants\\yellowshrub02" }, //
			{ "shrubgenericelderberryfa", "plants\\yellowshrub03" }, //
			{ "shrubgenericelderberrysu", "plants\\yellowshrub03" }, //
			{ "shrubgenericinkberryfa", "plants\\yellowshrub01" }, //
			{ "shrubgenericinkberrysu", "plants\\yellowshrub01" }, //
			{ "shrubhydrangeabluesu", "plants\\tundrashrub07" }, //
			{ "shrubinkberrysu", "plants\\deadshrub01" }, //
			{ "shrubjaphoneysucklesu", "plants\\deadshrub01" }, //
			//{ "shrubms14boxwood", "plants\\deadshrub01" }, //
			{ "shrubmugopine", "plants\\deadshrub01" }, //
			{ "shrubmugopinefa", "plants\\deadshrub01" }, //
			{ "shrubrhododendronsu", "plants\\deadshrub01" }, //
			{ "shrubseabuckthornsu", "plants\\deadshrub01" }, //
			{ "shrubvinemaplesnow", "plants\\vinemaple01snow" }, //
			{ "shrubvinemaplesu", "plants\\vinemaple01" }, //
			{ "shrubvinemaplewi", "plants\\deadshrub01" }, //
			{ "treeblacklocust", "trees\\reachtree01" }, //
			{ "treecamoranparadise01", "trees\\treeaspen01" }, //
			{ "treecamoranparadise02", "trees\\treeaspen01" }, //
			{ "treecamoranparadise04", "trees\\treeaspen01" }, //
			{ "treecottonwoodsu", "trees\\treeaspen01" }, //
			{ "treecpginkgo", "trees\\treeaspen02" }, //
			{ "treecpsnowgum", "trees\\treeaspen03" }, //
			{ "treecpswampcypressforest01", "trees\\treepineforestdead03" }, //
			{ "treedeodar", "trees\\treeaspen05" }, //
			{ "treedogwoodsu", "trees\\treeaspen04" }, //
			{ "treeeasthemlock", "trees\\treepineforest01" }, //
			{ "treeeasthemlockfa", "trees\\treepineforestdead01" }, //
			{ "treeeasthemlocksnow", "trees\\treepineforestsnow01" }, //
			{ "treeenglishholly01", "trees\\treepineforest02" }, //
			{ "treeenglishoakforest01fa", "trees\\treepineforestdead03" }, //
			{ "treeenglishoakforest01su", "trees\\treepineforest03" }, //
			{ "treeenglishoakforestfa", "trees\\treepineforestdead02" }, //
			{ "treeenglishoakforestsu", "trees\\treepineforest02" }, //
			{ "treeenglishoakfreesu", "trees\\treepineforest01" }, //
			{ "treeenglishoaksaplingfa", "trees\\reachtree01" }, //
			{ "treeenglishoaksaplingsu", "trees\\reachtree01" }, //
			{ "treeenglishoakunique01su", "trees\\reachtree01" }, //
			{ "treeenglishoakyoungfa", "trees\\reachtree01" }, //
			{ "treeenglishoakyoungsu", "trees\\reachtree01" }, //
			{ "treegcsycamoresu", "trees\\reachtree01" }, //
			{ "treeginkgo", "trees\\reachtree01" }, //
			{ "treegreypoplarforestfa", "trees\\treepineforest04" }, //
			{ "treegreypoplarforestsu", "trees\\treepineforest04" }, //
			{ "treeironwoodsu", "trees\\treeaspen01" }, //
			{ "treeironwoodwi", "trees\\treeaspen02" }, //
			{ "treejapanesemaple", "trees\\treeaspen05" }, //
			{ "treejuniper01", "trees\\treeaspen02" }, //
			{ "treekvatchburnt", "trees\\wrtempletree01" }, //
			//{ "treems14canvasfree", "trees\\treepineforest05" }, //
			//{ "treems14canvasfreesu", "trees\\treepineforest05" }, //
			//{ "treems14willowoakforest01su", "trees\\treepineforest05" }, //
			//{ "treems14willowoakforestsu", "trees\\treepineforest05" }, //
			//{ "treems14willowoakfreesu", "trees\\treepineforest05" }, //
			//{ "treems14willowoakyoungsu", "trees\\treepineforest05" }, //
			{ "treequakingaspenforest01fa", "trees\\treeaspen02" }, //
			{ "treequakingaspenforest01su", "trees\\treeaspen03" }, //
			{ "treequakingaspenforestfa", "trees\\treeaspen03" }, //
			{ "treequakingaspenforestsu", "trees\\treeaspen03" }, //
			{ "treequakingaspenfreesu", "trees\\treeaspen05" }, //
			{ "treequakingaspenyoungfa", "trees\\treeaspen04" }, //
			{ "treequakingaspenyoungsu", "trees\\treeaspen04" }, //
			{ "treeredwoodlarge", "trees\\treepineforest04" }, //
			{ "treeredwoodsmall", "trees\\treepineforest05" }, //
			{ "treescotchpineforest", "trees\\treepineforestdead02" }, //
			{ "treescotchpineforestsnow", "trees\\treepineforestdeadsnow03" }, //
			{ "treesilverbirchforest01fa", "trees\\treeaspen01" }, //
			{ "treesilverbirchforest01su", "trees\\treeaspen01" }, //
			{ "treesilverbirchforestfa", "trees\\treeaspen02" }, //
			{ "treesilverbirchforestsu", "trees\\treeaspen02" }, //
			{ "treesilverbirchfreesu", "trees\\treeaspen03" }, //
			{ "treesilverbirchyoungfa", "trees\\treeaspen04" }, //
			{ "treesilverbirchyoungsu", "trees\\treeaspen04" }, //
			{ "treesnowgumfree", "trees\\treepineforestdeadsnow03" }, //
			{ "treesugarmapleforest01fa", "trees\\treeaspen01" }, //
			{ "treesugarmapleforest01su", "trees\\treeaspen01" }, //
			{ "treesugarmapleforestfa", "trees\\treeaspen02" }, //
			{ "treesugarmapleforestsu", "trees\\treeaspen02" }, //
			{ "treesugarmaplefreesu", "trees\\treeaspen03" }, //
			{ "treesugarmaplesaplingfa", "trees\\treeaspen04" }, //
			{ "treesugarmaplesaplingsu", "trees\\treeaspen04" }, //
			{ "treesugarmapleyoungfa", "trees\\treeaspen05" }, //
			{ "treesugarmapleyoungsu", "trees\\treeaspen05" }, //
			{ "treeswampcypress", "trees\\reachtree01" }, //
			{ "treeswampcypressforest", "trees\\reachtree02" }, //
			{ "treeswampcypressforestfg08", "trees\\reachtree02" }, //
			{ "treetupelo", "trees\\treeaspen01" }, //
			{ "treeweepingwillowsu", "trees\\treeaspen01" }, //
			{ "treewhitepineforest", "trees\\treepineforest01" }, //
			{ "treewhitepineforest01", "trees\\treepineforest05" }, //
			{ "treewhitepineforest01fa", "trees\\treepineforest04" }, //
			{ "treewhitepineforestfa", "trees\\treepineforest03" }, //
			{ "treewhitepinefree", "trees\\treepineforest01" }, //
			{ "treewhitepinesnow", "trees\\treepineforestdeadsnow01" }, //
			{ "treewhitepineyoung", "trees\\treepineforest02" }, //
			{ "treewhitepineyoungfa", "trees\\treepineforest05" }, //
			{ "treewillowoakforest01su", "trees\\treeaspen03" }, //
			{ "treewillowoakforestsu", "trees\\treeaspen02" }, //
			{ "treewillowoakfreesu", "trees\\treeaspen01" }, //
			{ "treewillowoakyoungsu", "trees\\treeaspen04" }, //
			{ "treeyewforest", "trees\\wrtempletree02" }, //
			//////////////////////////////
			
			
			//Oblivion styles
			//tree whitepine free/forest [01] [young] su/fa/snow
			
			
			
			
			//Skyrim styles 
			//tree aspen 01-05
			//tree pineforest 01-05 su/spring
			//tree pineforestdead fa
			//tree pineforestdeadsnowl snow
			//tree pineforestdeadsnow deepwinter
			
			
			
			
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
