package esmj3d.j3d;

import java.util.Enumeration;

import javax.media.j3d.Behavior;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.View;
import javax.media.j3d.WakeupOnElapsedFrames;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

public class LODBillBoard extends Behavior
{
	//TODO: possibly a xpair of quads for super distant trees, with no transform? 
	//and a switch selector above? it would still need the behavior to trgiger select, but might lose one transfrom node?
	/**
	 * Specifies that rotation should be about the specified axis.
	 */
	public static final int ROTATE_ABOUT_AXIS = 0;

	/**
	 * Specifies that rotation should be about the specified point and
	 * that the children's Y-axis should match the view object's Y-axis.
	 */
	public static final int ROTATE_ABOUT_POINT = 1;

	// Wakeup condition for Billboard node
	private WakeupOnElapsedFrames wakeupFrameFast = new WakeupOnElapsedFrames(2, true);

	private WakeupOnElapsedFrames wakeupFrame5 = new WakeupOnElapsedFrames(5, true);

	private WakeupOnElapsedFrames wakeupFrame10 = new WakeupOnElapsedFrames(10, true);

	private WakeupOnElapsedFrames wakeupFrame50 = new WakeupOnElapsedFrames(50, true);

	// Specifies the billboard's mode of operation. One of ROTATE_AXIAL,
	// ROTATE_POINT_VIEW, or ROTATE_POINT_WORLD.
	private int mode = ROTATE_ABOUT_AXIS;

	// Axis about which to rotate.
	private Vector3f axis = new Vector3f(0.0f, 1.0f, 0.0f);

	private Point3f rotationPoint = new Point3f(0.0f, 0.0f, 1.0f);

	private Vector3d nAxis = new Vector3d(0.0, 1.0, 0.0); // normalized axis

	// TransformGroup to operate on.
	private TransformGroup tg = null;

	// reused temporaries
	private Point3d viewPosition = new Point3d();

	private Point3d yUpPoint = new Point3d();

	private Vector3d eyeVec = new Vector3d();

	private Vector3d yUp = new Vector3d();

	private Vector3d zAxis = new Vector3d();

	private Vector3d yAxis = new Vector3d();

	private Vector3d vector = new Vector3d();

	private AxisAngle4d aa = new AxisAngle4d();

	static final double EPSILON = 1.0e-6;

	/**
	 * Constructs a Billboard node with default parameters.
	 * The default values are as follows:
	 * <ul>
	 * alignment mode : ROTATE_ABOUT_AXIS<br>
	 * alignment axis : Y-axis (0,1,0)<br>
	 * rotation point : (0,0,1)<br>
	 * target transform group: null<br>
	 *</ul>
	 */
	public LODBillBoard()
	{
		nAxis.x = 0.0;
		nAxis.y = 1.0;
		nAxis.z = 0.0;
	}

	/**
	 * Constructs a Billboard node with default parameters that operates
	 * on the specified TransformGroup node.
	 * The default alignment mode is ROTATE_ABOUT_AXIS rotation with the axis
	 * pointing along the Y axis.
	 * @param tg the TransformGroup node that this Billboard
	 * node operates upon
	 */
	public LODBillBoard(TransformGroup tg)
	{
		this.tg = tg;
		nAxis.x = 0.0;
		nAxis.y = 1.0;
		nAxis.z = 0.0;

	}

	/**
	 * Constructs a Billboard node with the specified axis and mode
	 * that operates on the specified TransformGroup node.
	 * The specified axis must not be parallel to the <i>Z</i>
	 * axis--(0,0,<i>z</i>) for any value of <i>z</i>.  It is not
	 * possible for the +<i>Z</i> axis to point at the viewer's eye
	 * position by rotating about itself.  The target transform will
	 * be set to the identity if the axis is (0,0,<i>z</i>).
	 *
	 * @param tg the TransformGroup node that this Billboard
	 * node operates upon
	 * @param mode alignment mode, one of ROTATE_ABOUT_AXIS or 
	 * ROTATE_ABOUT_POINT
	 * @param axis the ray about which the billboard rotates
	 */
	public LODBillBoard(TransformGroup tg, int mode, Vector3f axis)
	{
		this.tg = tg;
		this.mode = mode;
		this.axis.set(axis);
		double invMag;
		invMag = 1.0 / Math.sqrt(axis.x * axis.x + axis.y * axis.y + axis.z * axis.z);
		nAxis.x = axis.x * invMag;
		nAxis.y = axis.y * invMag;
		nAxis.z = axis.z * invMag;

	}

	/**
	 * Constructs a Billboard node with the specified rotation point and mode
	 * that operates on the specified TransformGroup node.
	 * @param tg the TransformGroup node that this Billboard
	 * node operates upon
	 * @param mode alignment mode, one of ROTATE_ABOUT_AXIS or 
	 * ROTATE_ABOUT_POINT
	 * @param point the position about which the billboard rotates
	 */
	public LODBillBoard(TransformGroup tg, int mode, Point3f point)
	{
		this.tg = tg;
		this.mode = mode;
		this.rotationPoint.set(point);
	}

	/**
	 * Sets the alignment mode.
	 * @param mode one of: ROTATE_ABOUT_AXIS or ROTATE_ABOUT_POINT
	 */
	public void setAlignmentMode(int mode)
	{
		this.mode = mode;
	}

	/**
	 * Gets the alignment mode.
	 * @return one of: ROTATE_ABOUT_AXIS or ROTATE_ABOUT_POINT
	 */
	public int getAlignmentMode()
	{
		return this.mode;
	}

	/**
	 * Sets the alignment axis.
	 * The specified axis must not be parallel to the <i>Z</i>
	 * axis--(0,0,<i>z</i>) for any value of <i>z</i>.  It is not
	 * possible for the +<i>Z</i> axis to point at the viewer's eye
	 * position by rotating about itself.  The target transform will
	 * be set to the identity if the axis is (0,0,<i>z</i>).
	 *
	 * @param axis the ray about which the billboard rotates
	 */
	public void setAlignmentAxis(Vector3f axis)
	{
		this.axis.set(axis);
		double invMag;
		invMag = 1.0 / Math.sqrt(axis.x * axis.x + axis.y * axis.y + axis.z * axis.z);
		nAxis.x = axis.x * invMag;
		nAxis.y = axis.y * invMag;
		nAxis.z = axis.z * invMag;

	}

	/**
	 * Sets the alignment axis.
	 * The specified axis must not be parallel to the <i>Z</i>
	 * axis--(0,0,<i>z</i>) for any value of <i>z</i>.  It is not
	 * possible for the +<i>Z</i> axis to point at the viewer's eye
	 * position by rotating about itself.  The target transform will
	 * be set to the identity if the axis is (0,0,<i>z</i>).
	 *
	 * @param x the x component of the ray about which the billboard rotates
	 * @param y the y component of the ray about which the billboard rotates
	 * @param z the z component of the ray about which the billboard rotates
	 */
	public void setAlignmentAxis(float x, float y, float z)
	{
		this.axis.set(x, y, z);
		this.axis.set(axis);
		double invMag;
		invMag = 1.0 / Math.sqrt(axis.x * axis.x + axis.y * axis.y + axis.z * axis.z);
		nAxis.x = axis.x * invMag;
		nAxis.y = axis.y * invMag;
		nAxis.z = axis.z * invMag;

	}

	/**
	 * Gets the alignment axis and sets the parameter to this value.
	 * @param axis the vector that will contain the ray about which
	 * the billboard rotates
	 */
	public void getAlignmentAxis(Vector3f a)
	{
		a.set(this.axis);
	}

	/**
	 * Sets the rotation point.
	 * @param point the point about which the billboard rotates
	 */
	public void setRotationPoint(Point3f point)
	{
		this.rotationPoint.set(point);
	}

	/**
	 * Sets the rotation point.
	 * @param x the x component of the point about which the billboard rotates
	 * @param y the y component of the point about which the billboard rotates
	 * @param z the z component of the point about which the billboard rotates
	 */
	public void setRotationPoint(float x, float y, float z)
	{
		this.rotationPoint.set(x, y, z);
	}

	/**
	 * Gets the rotation point and sets the parameter to this value.
	 * @param point the position the Billboard rotates about
	 */
	public void getRotationPoint(Point3f point)
	{
		point.set(this.rotationPoint);
	}

	/**
	 * Sets the tranformGroup for this Billboard object.
	 * @param tg the transformGroup node which replaces the current
	 * transformGroup node for this Billboard
	 */
	public void setTarget(TransformGroup tg)
	{
		this.tg = tg;
	}

	/**
	 *  Returns a copy of the transformGroup associated with this Billboard.
	 *  @return the TranformGroup for this Billboard 
	 */
	public TransformGroup getTarget()
	{
		return (tg);
	}

	/**
	 * Initialize method that sets up initial wakeup criteria.
	 */
	public void initialize()
	{
		// Insert wakeup condition into queue
		wakeupOn(wakeupFrameFast);
	}

	/**
	 * Process stimulus method that computes appropriate transform.
	 * @param criteria an enumeration of the criteria that caused the
	 * stimulus
	 */
	@SuppressWarnings(
	{ "unchecked", "rawtypes" })
	public void processStimulus(Enumeration criteria)
	{
		double angle = 0.0;
		double sign;

		if (tg == null)
		{
			wakeupOn(wakeupFrame50);
			return;
		}

		//  get viewplatforms's location in virutal world
		View v = this.getView();
		if (v == null)
		{
			wakeupOn(wakeupFrame50);
			return;
		}
		Canvas3D canvas = v.getCanvas3D(0);
		boolean status;

		//		Transform3D xform = new Transform3D();
		//		Transform3D bbXform = new Transform3D();
		//		Transform3D prevTransform = new Transform3D();

		tg.getTransform(prevTransform);

		if (mode == ROTATE_ABOUT_AXIS)
		{ // rotate about axis
			canvas.getCenterEyeInImagePlate(viewPosition);
			canvas.getImagePlateToVworld(xform); // xform is imagePlateToLocal
			xform.transform(viewPosition);

			// get billboard's transform

			// since we are using getTransform() to get the transform
			// of the transformGroup, we need to use getLocalToVworld()
			// to get the localToVworld which includes the static transform

			tg.getLocalToVworld(xform);

			xform.invert(); // xform is now vWorldToLocal

			// transform the eye position into the billboard's coordinate system
			xform.transform(viewPosition);

			// eyeVec is a vector from the local origin to the eye pt in local
			eyeVec.set(viewPosition);
			eyeVec.normalize();

			// project the eye into the rotation plane
			status = projectToPlane(eyeVec, nAxis);

			// If the first project was successful .. 
			if (status)
			{
				// project the z axis into the rotation plane
				zAxis.x = 0.0;
				zAxis.y = 0.0;
				zAxis.z = 1.0;
				status = projectToPlane(zAxis, nAxis);
			}

			tg.getTransform(xform);
			if (status)
			{
				// compute the sign of the angle by checking if the cross product 
				// of the two vectors is in the same direction as the normal axis
				vector.cross(eyeVec, zAxis);
				if (vector.dot(nAxis) > 0.0)
				{
					sign = 1.0;
				}
				else
				{
					sign = -1.0;
				}

				// compute the angle between the projected eye vector and the 
				// projected z
				double dot = eyeVec.dot(zAxis);

				if (dot > 1.0f)
				{
					dot = 1.0f;
				}
				else if (dot < -1.0f)
				{
					dot = -1.0f;
				}

				angle = sign * Math.acos(dot);

				// use -angle because xform is to *undo* rotation by angle
				aa.x = nAxis.x;
				aa.y = nAxis.y;
				aa.z = nAxis.z;
				aa.angle = -angle;
				bbXform.set(aa);
				if (!prevTransform.epsilonEquals(bbXform, EPSILON))
				{
					// Optimization for Billboard since it use passive
					// behavior
					// set the transform on the Billboard TG
					tg.setTransform(bbXform);
				}
			}
			else
			{
				bbXform.setIdentity();
				if (!prevTransform.epsilonEquals(bbXform, EPSILON))
				{
					tg.setTransform(bbXform);
				}
			}

		}
		else
		{ // rotate about point
			// Need to rotate Z axis to point to eye, and Y axis to be 
			// parallel to view platform Y axis, rotating around rotation pt 

			//			Transform3D zRotate = new Transform3D();

			// get the eye point 
			canvas.getCenterEyeInImagePlate(viewPosition);

			// derive the yUp point
			yUpPoint.set(viewPosition);
			yUpPoint.y += 0.01; // one cm in Physical space

			// transform the points to the Billboard's space
			canvas.getImagePlateToVworld(xform); // xform is ImagePlateToVworld

			xform.transform(viewPosition);
			xform.transform(yUpPoint);

			// get billboard's transform

			// since we are using getTransform() to get the transform
			// of the transformGroup, we need to use getLocalToVworld()
			// to get the localToVworld which includes the static transform

			tg.getLocalToVworld(xform);

			xform.invert(); // xform is vWorldToLocal

			// transfom points to local coord sys
			xform.transform(viewPosition);
			xform.transform(yUpPoint);

			// Make a vector from viewPostion to 0,0,0 in the BB coord sys
			eyeVec.set(viewPosition);
			eyeVec.normalize();

			// create a yUp vector
			yUp.set(yUpPoint);
			yUp.sub(viewPosition);
			yUp.normalize();

			// find the plane to rotate z
			zAxis.x = 0.0;
			zAxis.y = 0.0;
			zAxis.z = 1.0;

			// rotation axis is cross product of eyeVec and zAxis
			vector.cross(eyeVec, zAxis); // vector is cross product

			// if cross product is non-zero, vector is rotation axis and 
			// rotation angle is acos(eyeVec.dot(zAxis)));
			double length = vector.length();

			if (length > 0.0001)
			{
				double dot = eyeVec.dot(zAxis);

				if (dot > 1.0f)
				{
					dot = 1.0f;
				}
				else if (dot < -1.0f)
				{
					dot = -1.0f;
				}

				angle = Math.acos(dot);
				aa.x = vector.x;
				aa.y = vector.y;
				aa.z = vector.z;
				aa.angle = -angle;
				zRotate.set(aa);
			}
			else
			{
				// no rotation needed, set to identity (scale = 1.0)
				zRotate.set(1.0);
			}

			// Transform the yAxis by zRotate
			yAxis.x = 0.0;
			yAxis.y = 1.0;
			yAxis.z = 0.0;
			zRotate.transform(yAxis);

			// project the yAxis onto the plane perp to the eyeVec 
			status = projectToPlane(yAxis, eyeVec);

			if (status)
			{
				// project the yUp onto the plane perp to the eyeVec 
				status = projectToPlane(yUp, eyeVec);
			}

			tg.getTransform(xform);
			if (status)
			{
				// rotation angle is acos(yUp.dot(yAxis));
				double dot = yUp.dot(yAxis);

				// Fix numerical error, otherwise acos return NULL
				if (dot > 1.0f)
				{
					dot = 1.0f;
				}
				else if (dot < -1.0f)
				{
					dot = -1.0f;
				}

				angle = Math.acos(dot);

				// check the sign by looking a the cross product vs the eyeVec 
				vector.cross(yUp, yAxis); // vector is cross product
				if (eyeVec.dot(vector) < 0)
				{
					angle *= -1;
				}
				aa.x = eyeVec.x;
				aa.y = eyeVec.y;
				aa.z = eyeVec.z;
				aa.angle = -angle;

				xform.set(aa); // xform is now yRotate

				// rotate around the rotation point
				vector.x = rotationPoint.x;
				vector.y = rotationPoint.y;
				vector.z = rotationPoint.z; // vector to translate to RP
				bbXform.set(vector); // translate to RP
				bbXform.mul(xform); // yRotate
				bbXform.mul(zRotate); // zRotate
				vector.scale(-1.0); // vector to translate back
				xform.set(vector); // xform to translate back 
				bbXform.mul(xform); // translate back

				if (!prevTransform.epsilonEquals(bbXform, EPSILON))
				{
					// set the transform on the Billboard TG
					tg.setTransform(bbXform);
				}
			}
			else
			{
				bbXform.setIdentity();
				if (!prevTransform.epsilonEquals(bbXform, EPSILON))
				{
					tg.setTransform(bbXform);
				}
			}
		}

		// I wager viewPosition is the eye point in the local transforms coordinates, I wager?
		//so let's just use the length for setting the wakeup
		double dist = Math.sqrt((viewPosition.x * viewPosition.x) + (viewPosition.y * viewPosition.y) + (viewPosition.z * viewPosition.z));
		//System.out.println("dist " + dist);
		//		Insert wakeup condition into queue
		if (dist < 40)
		{
			wakeupOn(wakeupFrameFast);			
		}
		else if (dist < 120)
		{
			wakeupOn(wakeupFrame5);
		}
		else if (dist < 280)
		{
			wakeupOn(wakeupFrame10);
		}
		else
		{
			wakeupOn(wakeupFrame50);
		}

	}

	// deburners
	private Transform3D xform = new Transform3D();

	private Transform3D bbXform = new Transform3D();

	private Transform3D prevTransform = new Transform3D();

	private Transform3D zRotate = new Transform3D();

	private boolean projectToPlane(Vector3d projVec, Vector3d planeVec)
	{
		double dis = planeVec.dot(projVec);
		projVec.x = projVec.x - planeVec.x * dis;
		projVec.y = projVec.y - planeVec.y * dis;
		projVec.z = projVec.z - planeVec.z * dis;

		double length = projVec.length();

		if (length < EPSILON)
		{
			return false;
		}
		projVec.scale(1 / length);
		return true;
	}

}
