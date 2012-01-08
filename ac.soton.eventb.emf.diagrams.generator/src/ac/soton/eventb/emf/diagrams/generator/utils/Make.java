/**
 * Copyright (c) 2012 University of Southampton.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package ac.soton.eventb.emf.diagrams.generator.utils;

import java.util.Collections;
import java.util.List;

import org.eclipse.emf.ecore.EStructuralFeature;
import org.eventb.emf.core.EventBElement;
import org.eventb.emf.core.context.Axiom;
import org.eventb.emf.core.context.CarrierSet;
import org.eventb.emf.core.context.Constant;
import org.eventb.emf.core.context.Context;
import org.eventb.emf.core.context.ContextFactory;
import org.eventb.emf.core.machine.Action;
import org.eventb.emf.core.machine.Convergence;
import org.eventb.emf.core.machine.Event;
import org.eventb.emf.core.machine.Guard;
import org.eventb.emf.core.machine.Invariant;
import org.eventb.emf.core.machine.MachineFactory;
import org.eventb.emf.core.machine.Parameter;
import org.eventb.emf.core.machine.Variable;

import ac.soton.eventb.emf.diagrams.generator.GenerationDescriptor;

/**
 * This is a collection of static convenience methods for making objects that are needed in generator Rules
 * 
 */

public class Make {
	
	public static GenerationDescriptor descriptor(EventBElement parent, EStructuralFeature feature, Object value, int priority){
		return new GenerationDescriptor(parent,feature,value,Integer.valueOf(priority));
	}
	
	 public static Variable variable(String name, String comment) {
		Variable v =  MachineFactory.eINSTANCE.createVariable();
	    v.setName(name);
	    v.setLocalGenerated(true);
	    v.setComment(comment);
	    return v;  
	 }
	  
	public static Invariant invariant(String name, String predicate,String comment) {
		Invariant i =  MachineFactory.eINSTANCE.createInvariant();
	    i.setName(name);
	    i.setPredicate(predicate);
	    i.setLocalGenerated(true);
	    i.setComment(comment);
	    return i;  
	}

	public static Object event(String name) {
		return event(name, false, Convergence.ORDINARY, Collections.<String> emptyList(), "");
	}
		
	public static Object event(String name, boolean extended, Convergence convergence, List<String> refinesNames, String comment) {
		Event e =  MachineFactory.eINSTANCE.createEvent();
	    e.setName(name);
	    e.setExtended(extended);
	    e.setConvergence(convergence);
	    e.getRefinesNames().addAll(refinesNames);
	    e.setLocalGenerated(true);
	    e.setComment(comment);
	    return e;  
	}

	public static Object parameter(String name, String comment) {
		Parameter p =  MachineFactory.eINSTANCE.createParameter();
	    p.setName(name);
	    p.setLocalGenerated(true);
	    p.setComment(comment);
	    return p;  
	}
	
	public static Object guard(String name, String predicate){
		return guard(name, false, predicate, "");
	}
	
	public static Object guard(String name, boolean theorem, String invariant, String comment) {
		Guard g =  MachineFactory.eINSTANCE.createGuard();
	    g.setName(name);
	    g.setTheorem(theorem);
	    g.setPredicate(invariant);
	    g.setLocalGenerated(true);
	    g.setComment(comment);
	    return g;  
	}
	
	public static Object action(String name, String expression){
		return action(name, expression, "");
	}
	
	public static Object action(String name, String expression, String comment) {
		Action a =  MachineFactory.eINSTANCE.createAction();
	    a.setName(name);
	    a.setAction(expression);
	    a.setLocalGenerated(true);
	    a.setComment(comment);
	    return a;  
	}

	public static Object context(String name, String comment) {
		Context ctx =  ContextFactory.eINSTANCE.createContext();
	    ctx.setName(name);
	    ctx.setLocalGenerated(true);
	    ctx.setComment(comment);
	    return ctx;
	}

	public static Object set(String name, String comment) {
		CarrierSet set =  ContextFactory.eINSTANCE.createCarrierSet();
	    set.setName(name);
	    set.setLocalGenerated(true);
	    set.setComment(comment);
	    return set;
	}

	public static Object constant(String name, String comment) {
		Constant constant =  ContextFactory.eINSTANCE.createConstant();
	    constant.setName(name);
	    constant.setLocalGenerated(true);
	    constant.setComment(comment);
	    return constant;
	}
	
	public static Axiom axiom(String name, String predicate,String comment) {
		Axiom axm =  ContextFactory.eINSTANCE.createAxiom();
	    axm.setName(name);
	    axm.setPredicate(predicate);
	    axm.setLocalGenerated(true);
	    axm.setComment(comment);
	    return axm;  
	}
	
}
