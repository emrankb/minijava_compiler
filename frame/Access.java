/* Author: Emran Kebede
 * Spring 2021
 */

package frame;

import tree.Exp;

public abstract class Access {
	public abstract Exp exp(Exp framePtr);
}
