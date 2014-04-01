/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cs304;

/**
 *
 * @author Collin
 */
public class FiveTuple<V,W,X,Y,Z> {
        public final V v;
        public final W w;
	public final X x; 
	public final Y y;
	public final Z z;

	public FiveTuple(V v, W w, X x, Y y, Z z) {
                this.v = v;
		this.w = w;
		this.x = x;
		this.y = y;
		this.z = z;
	}
}
