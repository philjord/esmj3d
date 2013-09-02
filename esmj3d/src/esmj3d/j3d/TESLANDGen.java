package esmj3d.j3d;

import javax.vecmath.Color4f;
import javax.vecmath.TexCoord2f;
import javax.vecmath.Vector3f;

import org.j3d.geom.GeometryData;
import org.j3d.geom.InvalidArraySizeException;

/**
	 * A generator that takes a set of height values as a grid and turns it into
	 * geometry.

	 * Points are defined in the height arrays in width first order. Normals, are
	 * always smooth blended.
	 *
	 */

public class TESLANDGen
{
	/** Current width of the terrain */
	private float terrainWidth;

	/** Depth of the terrain to generate */
	private float terrainDepth;

	/** Number of points in the width direction */
	private int widthPoints;

	/** Number of points in the depth direction */
	private int depthPoints;

	/** The points to use as a 2D array. */
	private float[][] arrayHeights;

	/** The points to use as a 2D array. */
	private Vector3f[][] arrayNormals;

	/** The colors to use as a 2D array. */
	private Color4f[][] arrayColors;

	/** The points to use as a 2D array. */
	private TexCoord2f[][] arrayTexCoords;

	/** The number of terrain coordinates in use */
	private int numTerrainValues;

	/** The array holding all of the vertices after use */
	private float[] terrainCoordinates;

	/** The array holding all of the normals after use */
	private float[] terrainNormals;

	/** The array holding all of the normals after use */
	private float[] terrainColors;

	/** The array holding all of the normals after use */
	private float[] terrainTexCoords;

	/** The number of quads in the terrain */
	private int facetCount;

	/**
	 * @param w The width of the terrain
	 * @param d The depth of the terrain
	 * @param wPnts The number of heights in the width
	 * @param dPnts The number of heights in the depth
	 * @param heights The array of height values to use
	 * @param normals  
	 * @param colors
	 * @param texCoords
	 * @throws IllegalArgumentException One of the points were <= 1 or the dimensions are non-positive
	 */

	//TODO: sounds like this gy might be an ineffcient way to make teh terrain
	public TESLANDGen(float w, float d, int wPnts, int dPnts, float[][] heights, Vector3f[][] normals, Color4f[][] colors,
			TexCoord2f[][] texCoords)
	{
		if ((wPnts < 2) || (dPnts < 2))
			throw new IllegalArgumentException("Point count <= 1");

		if ((w <= 0) || (d <= 0))
			throw new IllegalArgumentException("Dimension <= 0");

		terrainWidth = w;
		terrainDepth = d;
		widthPoints = wPnts;
		depthPoints = dPnts;

		facetCount = (depthPoints - 1) * (widthPoints - 1);

		arrayHeights = heights;
		arrayNormals = normals;
		arrayColors = colors;
		arrayTexCoords = texCoords;
	}

	/**
	 * Generate a new set of points for an unindexed quad array
	 *
	 * @param data The data to base the calculations on
	 * @throws InvalidArraySizeException The array is not big enough to contain
	 *   the requested geometry
	 */
	public void generateUnindexedTriangles(GeometryData data) throws InvalidArraySizeException
	{
		generateUnindexedTriCoordinates(data);
		generateUnindexedTriNormals(data);
		//generateUnindexedTriColors(data);
		//generateUnindexedTriTexCoords(data);
	}

	/**
	 * Generate a new set of points for an indexed triangle array
	 *
	 * @param data The data to base the calculations on
	 * @throws InvalidArraySizeException The array is not big enough to contain
	 *   the requested geometry
	 */
	public void generateIndexedTriangles(GeometryData data) throws InvalidArraySizeException
	{
		generateIndexedCoordinates(data);
		generateIndexedNormals(data);
		generateIndexedColors(data);
		generateIndexedTexCoords(data);

		// now let's do the index list
		int index_size = data.vertexCount * 6;

		data.indexes = new int[index_size];

		int[] indexes = data.indexes;
		data.indexesCount = index_size;
		int idx = 0;
		int vtx = 0;

		// each face consists of an anti-clockwise
		for (int i = facetCount; --i >= 0;)
		{
			// triangle 1
			indexes[idx++] = vtx;
			indexes[idx++] = vtx + widthPoints + 1;
			indexes[idx++] = vtx + 1;

			// triangle 2
			indexes[idx++] = vtx + widthPoints;
			indexes[idx++] = vtx + widthPoints + 1;
			indexes[idx++] = vtx;

			vtx++;

			if ((i % (widthPoints - 1)) == 0)
				vtx++;
		}
	}

	/**
	 * Generate a new set of points for a triangle strip array. There is one
	 * strip for the side and one strip each for the ends.
	 *
	 * @param data The data to base the calculations on
	 * @throws InvalidArraySizeException The array is not big enough to contain
	 *   the requested geometry
	 */
	public void generateTriangleStrips(GeometryData data) throws InvalidArraySizeException
	{
		generateUnindexedTriStripCoordinates(data);
		generateUnindexedTriStripNormals(data);
		generateUnindexedTriStripColors(data);
		generateUnindexedTriStripTexCoords(data);

		int num_strips = depthPoints - 1;

		data.stripCounts = new int[num_strips];

		for (int i = num_strips; --i >= 0;)
			data.stripCounts[i] = widthPoints * 2;
	}

	/**
	 * Generate a new set of points for an indexed triangle strip array. We
	 * build the strip from the existing points starting by working around the
	 * side and then doing the top and bottom. To create the ends we start at
	 * on radius point and then always refer to the center for each second
	 * item. This wastes every second triangle as a degenerate triangle, but
	 * the gain is less strips needing to be transmitted - ie less memory
	 * usage.
	 *
	 * @param data The data to base the calculations on
	 * @throws InvalidArraySizeException The array is not big enough to contain
	 *   the requested geometry
	 */
	public void generateIndexedTriangleStrips(GeometryData data) throws InvalidArraySizeException
	{
		generateIndexedCoordinates(data);
		generateIndexedNormals(data);
		generateIndexedColors(data);
		generateIndexedTexCoords(data);

		// now let's do the index list
		int index_size = widthPoints * (depthPoints - 1) * 2;
		int num_strips = depthPoints - 1;

		data.indexes = new int[index_size];

		data.stripCounts = new int[num_strips];

		int[] indexes = data.indexes;
		int[] stripCounts = data.stripCounts;
		data.indexesCount = index_size;
		data.numStrips = num_strips;
		int idx = 0;
		int vtx = 0;
		int total_points = widthPoints * (depthPoints - 1);

		// The side is one big strip
		for (int i = total_points; --i >= 0;)
		{
			indexes[idx++] = vtx;
			indexes[idx++] = vtx + widthPoints;

			vtx++;
		}

		for (int i = num_strips; --i >= 0;)
			stripCounts[i] = widthPoints * 2;
	}

	/**
	 * Generates new set of unindexed points for triangles. The array consists
	 * of the side coordinates, followed by the top and bottom.
	 *
	 * @param data The data to base the calculations on
	 * @throws InvalidArraySizeException The array is not big enough to contain
	 *   the requested geometry
	 */
	private void generateUnindexedTriCoordinates(GeometryData data) throws InvalidArraySizeException
	{
		int vtx_cnt = depthPoints * widthPoints * 6;

		data.coordinates = new float[vtx_cnt * 3];

		float[] coords = data.coordinates;
		data.vertexCount = vtx_cnt;

		regenerateBase();

		int count = 0;
		int i = 0;
		int base_count = 0;
		int width_inc = widthPoints * 3;

		// Start of with one less row (width) here because we don't have two
		// sets of coordinates for those.
		for (i = facetCount; --i >= 0;)
		{
			// triangle 1
			coords[count++] = terrainCoordinates[base_count];
			coords[count++] = terrainCoordinates[base_count + 1];
			coords[count++] = terrainCoordinates[base_count + 2];

			coords[count++] = terrainCoordinates[base_count + width_inc];
			coords[count++] = terrainCoordinates[base_count + width_inc + 1];
			coords[count++] = terrainCoordinates[base_count + width_inc + 2];

			coords[count++] = terrainCoordinates[base_count + 3];
			coords[count++] = terrainCoordinates[base_count + 4];
			coords[count++] = terrainCoordinates[base_count + 5];

			// triangle 2
			coords[count++] = terrainCoordinates[base_count + width_inc];
			coords[count++] = terrainCoordinates[base_count + width_inc + 1];
			coords[count++] = terrainCoordinates[base_count + width_inc + 2];

			coords[count++] = terrainCoordinates[base_count + width_inc + 3];
			coords[count++] = terrainCoordinates[base_count + width_inc + 4];
			coords[count++] = terrainCoordinates[base_count + width_inc + 5];

			coords[count++] = terrainCoordinates[base_count + 3];
			coords[count++] = terrainCoordinates[base_count + 4];
			coords[count++] = terrainCoordinates[base_count + 5];

			base_count += 3;

			if ((i % (widthPoints - 1)) == 0)
				base_count += 3;
		}

	}

	/**
	 * Generate a new set of normals for a normal set of unindexed points.
	 * Smooth normals are used for the sides at the average between the faces.
	 * Bottom normals always point down.
	 * <p>
	 * This must always be called after the coordinate generation.
	 *
	 * @param data The data to base the calculations on
	 * @throws InvalidArraySizeException The array is not big enough to contain
	 *   the requested geometry
	 */
	private void generateUnindexedTriNormals(GeometryData data) throws InvalidArraySizeException
	{
		int vtx_cnt = data.vertexCount * 3;

		data.normals = new float[vtx_cnt];

		regenerateNormals();

		int i = 0;
		int count = 0;
		int base_count = 0;
		int width_inc = widthPoints * 3;
		float[] normals = data.normals;

		// Start of with one less row (width) here because we don't have two
		// sets of coordinates for those.
		for (i = facetCount; --i >= 0;)
		{
			// triangle 1
			normals[count++] = terrainNormals[base_count];
			normals[count++] = terrainNormals[base_count + 1];
			normals[count++] = terrainNormals[base_count + 2];

			normals[count++] = terrainNormals[base_count + width_inc];
			normals[count++] = terrainNormals[base_count + width_inc + 1];
			normals[count++] = terrainNormals[base_count + width_inc + 2];

			normals[count++] = terrainNormals[base_count + 3];
			normals[count++] = terrainNormals[base_count + 4];
			normals[count++] = terrainNormals[base_count + 5];

			// triangle 2
			normals[count++] = terrainNormals[base_count + width_inc];
			normals[count++] = terrainNormals[base_count + width_inc + 1];
			normals[count++] = terrainNormals[base_count + width_inc + 2];

			normals[count++] = terrainNormals[base_count + width_inc + 3];
			normals[count++] = terrainNormals[base_count + width_inc + 4];
			normals[count++] = terrainNormals[base_count + width_inc + 5];

			normals[count++] = terrainNormals[base_count + 3];
			normals[count++] = terrainNormals[base_count + 4];
			normals[count++] = terrainNormals[base_count + 5];

			base_count += 3;

			if ((i % (widthPoints - 1)) == 0)
				base_count += 3;
		}
	}

	/**
	 * Generates new set of unindexed points for triangles strips. The array
	 * consists of one strip per width row.
	 *
	 * @param data The data to base the calculations on
	 * @throws InvalidArraySizeException The array is not big enough to contain
	 *   the requested geometry
	 */
	private void generateUnindexedTriStripCoordinates(GeometryData data) throws InvalidArraySizeException
	{
		int vtx_cnt = widthPoints * (depthPoints - 1) * 2;

		data.coordinates = new float[vtx_cnt * 3];

		float[] coords = data.coordinates;
		data.vertexCount = vtx_cnt;

		regenerateBase();

		int i;
		int count = 0;
		int base_count = 0;
		int width_inc = widthPoints * 3;
		int total_points = widthPoints * (depthPoints - 1);

		// Start of with one less row (width) here because we don't have two
		// sets of coordinates for those.
		for (i = total_points; --i >= 0;)
		{
			coords[count++] = terrainCoordinates[base_count];
			coords[count++] = terrainCoordinates[base_count + 1];
			coords[count++] = terrainCoordinates[base_count + 2];

			coords[count++] = terrainCoordinates[base_count + width_inc];
			coords[count++] = terrainCoordinates[base_count + width_inc + 1];
			coords[count++] = terrainCoordinates[base_count + width_inc + 2];

			base_count += 3;
		}

	}

	/**
	 * Generate a new set of normals for a normal set of unindexed points.
	 * Smooth normals are used for the sides at the average between the faces.
	 * Bottom normals always point down.
	 * <p>
	 * This must always be called after the coordinate generation.
	 *
	 * @param data The data to base the calculations on
	 * @throws InvalidArraySizeException The array is not big enough to contain
	 *   the requested geometry
	 */
	private void generateUnindexedTriStripNormals(GeometryData data) throws InvalidArraySizeException
	{
		int vtx_cnt = data.vertexCount * 3;

		data.normals = new float[vtx_cnt];

		regenerateNormals();

		int i;
		float[] normals = data.normals;
		int count = 0;
		int base_count = 0;
		int width_inc = widthPoints * 3;
		int total_points = widthPoints * (depthPoints - 1);

		// Start of with one less row (width) here because we don't have two
		// sets of coordinates for those.
		for (i = total_points; --i >= 0;)
		{
			normals[count++] = terrainNormals[base_count];
			normals[count++] = terrainNormals[base_count + 1];
			normals[count++] = terrainNormals[base_count + 2];

			normals[count++] = terrainNormals[base_count + width_inc];
			normals[count++] = terrainNormals[base_count + width_inc + 1];
			normals[count++] = terrainNormals[base_count + width_inc + 2];

			base_count += 3;
		}
	}

	private void generateUnindexedTriStripColors(GeometryData data) throws InvalidArraySizeException
	{
		int vtx_cnt = data.vertexCount * 4;

		data.colors = new float[vtx_cnt];

		regenerateColors();

		int i;
		float[] colors = data.colors;
		int count = 0;
		int base_count = 0;
		int width_inc = widthPoints * 4;
		int total_points = widthPoints * (depthPoints - 1);

		// Start of with one less row (width) here because we don't have two
		// sets of coordinates for those.
		for (i = total_points; --i >= 0;)
		{
			colors[count++] = terrainColors[base_count];
			colors[count++] = terrainColors[base_count + 1];
			colors[count++] = terrainColors[base_count + 2];
			colors[count++] = terrainColors[base_count + 3];

			colors[count++] = terrainColors[base_count + width_inc];
			colors[count++] = terrainColors[base_count + width_inc + 1];
			colors[count++] = terrainColors[base_count + width_inc + 2];
			colors[count++] = terrainColors[base_count + width_inc + 3];

			base_count += 4;
		}
	}

	private void generateUnindexedTriStripTexCoords(GeometryData data) throws InvalidArraySizeException
	{
		int vtx_cnt = data.vertexCount * 2;

		data.textureCoordinates = new float[vtx_cnt];

		regenerateTexCoords();

		int i;
		float[] textureCoordinates = data.textureCoordinates;
		int count = 0;
		int base_count = 0;
		int width_inc = widthPoints * 2;
		int total_points = widthPoints * (depthPoints - 1);

		// Start of with one less row (width) here because we don't have two
		// sets of coordinates for those.
		for (i = total_points; --i >= 0;)
		{
			textureCoordinates[count++] = terrainTexCoords[base_count];
			textureCoordinates[count++] = terrainTexCoords[base_count + 1];

			textureCoordinates[count++] = terrainTexCoords[base_count + width_inc];
			textureCoordinates[count++] = terrainTexCoords[base_count + width_inc + 1];

			base_count += 2;
		}
	}

	/**
		 * Generates new set of indexed points for triangles or quads. The array
		 * consists of the side coordinates, followed by the center for top, then
		 * its points then the bottom center and its points. We do this as they
		 * use a completely different set of normals. The side
		 * coordinates are interleved as top and then bottom values.
		 *
		 * @param data The data to base the calculations on
		 * @throws InvalidArraySizeException The array is not big enough to contain
		 *   the requested geometry
		 */
	private void generateIndexedCoordinates(GeometryData data) throws InvalidArraySizeException
	{
		int vtx_cnt = widthPoints * depthPoints;

		data.coordinates = new float[vtx_cnt * 3];

		float[] coords = data.coordinates;
		data.vertexCount = vtx_cnt;

		regenerateBase();

		System.arraycopy(terrainCoordinates, 0, coords, 0, numTerrainValues);

	}

	/**
	 * Generate a new set of normals for a normal set of indexed points.
	 * Smooth normals are used for the sides at the average between the faces.
	 * Bottom normals always point down.
	 * <p>
	 * This must always be called after the coordinate generation.
	 *
	 * @param data The data to base the calculations on
	 * @throws InvalidArraySizeException The array is not big enough to contain
	 *   the requested geometry
	 */
	private void generateIndexedNormals(GeometryData data) throws InvalidArraySizeException
	{
		int vtx_cnt = data.vertexCount * 3;

		data.normals = new float[vtx_cnt];

		regenerateNormals();

		System.arraycopy(terrainNormals, 0, data.normals, 0, numTerrainValues);
	}

	private void generateIndexedColors(GeometryData data) throws InvalidArraySizeException
	{
		int vtx_cnt = data.vertexCount * 4;

		data.colors = new float[vtx_cnt];

		regenerateColors();

		System.arraycopy(terrainColors, 0, data.colors, 0, widthPoints * depthPoints * 4);
	}

	private void generateIndexedTexCoords(GeometryData data) throws InvalidArraySizeException
	{
		int vtx_cnt = data.vertexCount * 2;

		data.textureCoordinates = new float[vtx_cnt];

		regenerateTexCoords();

		System.arraycopy(terrainTexCoords, 0, data.textureCoordinates, 0, widthPoints * depthPoints * 2);
	}

	/**
	 * Regenerate the base coordinate points. These are the flat circle that
	 * makes up the base of the code. The coordinates are generated based on
	 * the 2 PI divided by the number of facets to generate.
	 */
	private final void regenerateBase()
	{
		numTerrainValues = widthPoints * depthPoints * 3;

		terrainCoordinates = new float[numTerrainValues];

		float d = -terrainDepth / 2;
		float w = -terrainWidth / 2;
		float width_inc = terrainWidth / (widthPoints - 1);
		float depth_inc = terrainDepth / (depthPoints - 1);

		int count = 0;

		for (int i = 0; i < depthPoints; i++)
		{
			for (int j = 0; j < widthPoints; j++)
			{
				terrainCoordinates[count++] = w;
				terrainCoordinates[count++] = arrayHeights[i][j];
				terrainCoordinates[count++] = d;

				w += width_inc;
			}

			d += depth_inc;
			w = -terrainWidth / 2;
		}

	}

	private final void regenerateColors()
	{

		terrainColors = new float[widthPoints * depthPoints * 4];

		int count = 0;

		for (int i = 0; i < depthPoints; i++)
		{
			for (int j = 0; j < widthPoints; j++)
			{
				if (arrayColors != null)
				{
					terrainColors[count++] = arrayColors[i][j].x;
					terrainColors[count++] = arrayColors[i][j].y;
					terrainColors[count++] = arrayColors[i][j].z;
					terrainColors[count++] = arrayColors[i][j].w;
				}
				else
				{
					terrainColors[count++] = 1.0f;
					terrainColors[count++] = 1.0f;
					terrainColors[count++] = 1.0f;
					terrainColors[count++] = 1.0f;
				}
			}
		}

	}

	private final void regenerateTexCoords()
	{

		terrainTexCoords = new float[widthPoints * depthPoints * 2];

		int count = 0;

		for (int i = 0; i < depthPoints; i++)
		{
			for (int j = 0; j < widthPoints; j++)
			{
				if (arrayTexCoords != null)
				{
					terrainTexCoords[count++] = arrayTexCoords[i][j].x;
					terrainTexCoords[count++] = arrayTexCoords[i][j].y;
				}
				else
				{
					terrainTexCoords[count++] = 1.0f;
					terrainTexCoords[count++] = 1.0f;
				}
			}
		}

	}

	/**
	 * Regenerate the base normals points. These are the flat circle that
	 * makes up the base of the code. The normals are generated based the
	 * smoothing of normal averages for interior points. Around the edges,
	 * we use the average of the edge value polygons.
	 */
	private final void regenerateNormals()
	{

		terrainNormals = new float[numTerrainValues];

		if (arrayNormals != null)
		{
			int count = 0;

			for (int i = 0; i < depthPoints; i++)
			{
				for (int j = 0; j < widthPoints; j++)
				{
					Vector3f n = arrayNormals[i][j];
					terrainNormals[count++] = n.x;
					terrainNormals[count++] = n.y;
					terrainNormals[count++] = n.z;
				}
			}
		}
	}

	/*	private static float[] trianglizeTexture2D(int widthPoints, int depthPoints, TexCoord2f[][] texCoords)
		{
			//TODO: this is called by nothing, remove?
			int facetCount = (depthPoints - 1) * (widthPoints - 1);
	
			float[] coords = new float[depthPoints * widthPoints * 6 * 2];
	
			int count = 0;
	
			float[] terrainTexcoords = new float[depthPoints * widthPoints * 2];
			for (int i = 0; i < depthPoints; i++)
			{
				for (int j = 0; j < widthPoints; j++)
				{
					TexCoord2f t = texCoords[i][j];
					terrainTexcoords[count++] = t.x;
					terrainTexcoords[count++] = t.y;
				}
			}
	
			count = 0;
			int base_count = 0;
			int width_inc = widthPoints * 2;
	
			// Start of with one less row (width) here because we don't have two
			// sets of coordinates for those.
			for (int i = facetCount; --i >= 0;)
			{
				// triangle 1
				coords[count++] = terrainTexcoords[base_count];
				coords[count++] = terrainTexcoords[base_count + 1];
	
				coords[count++] = terrainTexcoords[base_count + width_inc];
				coords[count++] = terrainTexcoords[base_count + width_inc + 1];
	
				coords[count++] = terrainTexcoords[base_count + 2];
				coords[count++] = terrainTexcoords[base_count + 3];
	
				// triangle 2
				coords[count++] = terrainTexcoords[base_count + width_inc];
				coords[count++] = terrainTexcoords[base_count + width_inc + 1];
	
				coords[count++] = terrainTexcoords[base_count + width_inc + 2];
				coords[count++] = terrainTexcoords[base_count + width_inc + 3];
	
				coords[count++] = terrainTexcoords[base_count + 2];
				coords[count++] = terrainTexcoords[base_count + 3];
	
				base_count += 2;
	
				if ((i % (widthPoints - 1)) == 0)
					base_count += 2;
			}
	
			return coords;
		}*/

}
