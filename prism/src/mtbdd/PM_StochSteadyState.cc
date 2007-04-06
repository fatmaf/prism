//==============================================================================
//	
//	Copyright (c) 2002-
//	Authors:
//	* Dave Parker <dxp@cs.bham.uc.uk> (University of Birmingham)
//	
//------------------------------------------------------------------------------
//	
//	This file is part of PRISM.
//	
//	PRISM is free software; you can redistribute it and/or modify
//	it under the terms of the GNU General Public License as published by
//	the Free Software Foundation; either version 2 of the License, or
//	(at your option) any later version.
//	
//	PRISM is distributed in the hope that it will be useful,
//	but WITHOUT ANY WARRANTY; without even the implied warranty of
//	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//	GNU General Public License for more details.
//	
//	You should have received a copy of the GNU General Public License
//	along with PRISM; if not, write to the Free Software Foundation,
//	Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//	
//==============================================================================

// includes
#include "PrismMTBDD.h"
#include <math.h>
#include <util.h>
#include <cudd.h>
#include <dd.h>
#include <odd.h>
#include "PrismMTBDDGlob.h"

//------------------------------------------------------------------------------

JNIEXPORT jint JNICALL Java_mtbdd_PrismMTBDD_PM_1StochSteadyState
(
JNIEnv *env,
jclass cls,
jint tr,		// trans matrix
jint od,		// odd
jint in,		// init soln
jint rv,		// row vars
jint num_rvars,
jint cv,		// col vars
jint num_cvars
)
{
	// cast function parameters
	DdNode *trans = (DdNode *)tr;	// trans matrix
	ODDNode *odd = (ODDNode *)od;	// odd
	DdNode *init = (DdNode *)in;	// init soln
	DdNode **rvars = (DdNode **)rv; // row vars
	DdNode **cvars = (DdNode **)cv; // col vars
	// mtbdds
	DdNode *diags, *q, *a, *b, *soln, *tmp;
	// misc
	int i;
	double deltat, d;
	
	// compute diagonals
	Cudd_Ref(trans);
	diags = DD_SumAbstract(ddman, trans, cvars, num_rvars);
	diags = DD_Apply(ddman, APPLY_TIMES, diags, DD_Constant(ddman, -1));
	
	// if diagonal is 0 set it to -1
	// (fix for when we are solving subsystem e.g. BSCC)
	Cudd_Ref(diags);
	diags = DD_ITE(ddman, DD_LessThan(ddman, diags, 0), diags, DD_Constant(ddman, -1));
	
	// build generator matrix q from trans and diags
	// note that any self loops are effectively removed because we include their rates
	// in the 'diags' row sums and then subtract these from the original rate matrix
	Cudd_Ref(trans);
	Cudd_Ref(diags);
	q = DD_Apply(ddman, APPLY_PLUS, trans, DD_Apply(ddman, APPLY_TIMES, DD_Identity(ddman, rvars, cvars, num_rvars), diags));
	
	// if we are going to solve with the power method, we have to modify the matrix a bit
	if (lin_eq_method == LIN_EQ_METHOD_POWER) {
		// choose deltat
		deltat = -0.99 / DD_FindMin(ddman, diags);
		// build iteration matrix
		Cudd_Ref(q);
		a = DD_Apply(ddman, APPLY_PLUS, DD_Apply(ddman, APPLY_TIMES, DD_Constant(ddman, deltat), q), DD_Identity(ddman, rvars, cvars, num_rvars));
	}
	else {
		Cudd_Ref(q);
		a = q;
	}
	
	// b vector is all zeros
	b = DD_Constant(ddman, 0);
	
	// call iterative method
	soln = NULL;
	switch (lin_eq_method) {
		case LIN_EQ_METHOD_POWER:
			soln = (DdNode *)Java_mtbdd_PrismMTBDD_PM_1Power(env, cls, (jint)odd, (jint)rvars, num_rvars, (jint)cvars, num_cvars, (jint)a, (jint)b, (jint)init, true); break;
		case LIN_EQ_METHOD_JACOBI:
			soln = (DdNode *)Java_mtbdd_PrismMTBDD_PM_1JOR(env, cls, (jint)odd, (jint)rvars, num_rvars, (jint)cvars, num_cvars, (jint)a, (jint)b, (jint)init, true, 1.0); break;
		case LIN_EQ_METHOD_JOR:
			soln = (DdNode *)Java_mtbdd_PrismMTBDD_PM_1JOR(env, cls, (jint)odd, (jint)rvars, num_rvars, (jint)cvars, num_cvars, (jint)a, (jint)b, (jint)init, true, lin_eq_method_param); break;
	}
	
	// normalise
	if (soln != NULL) {
		Cudd_Ref(soln);
		soln = DD_Apply(ddman, APPLY_DIVIDE, soln, DD_SumAbstract(ddman, soln, rvars, num_rvars));
	}
	
	// free memory
	Cudd_RecursiveDeref(ddman, diags);
	Cudd_RecursiveDeref(ddman, q);
	Cudd_RecursiveDeref(ddman, a);
	Cudd_RecursiveDeref(ddman, b);
	
	return (int)soln;
}

//------------------------------------------------------------------------------
