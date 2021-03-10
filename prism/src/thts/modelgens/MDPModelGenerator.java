package thts.modelgens;

import java.util.List;

import parser.State;
import parser.VarList;
import parser.type.Type;
import prism.DefaultModelGenerator;
import prism.ModelType;
import prism.PrismException;
import simulator.ModulesFileModelGenerator;

public class MDPModelGenerator extends DefaultModelGenerator {

	ModulesFileModelGenerator modGen;
	public MDPModelGenerator(ModulesFileModelGenerator modGen)
	{
		this.modGen = modGen;
	}
	@Override
	public State getInitialState() throws PrismException {
		// TODO Auto-generated method stub
//		return null;
		return modGen.getInitialState();
	}

	@Override
	public void exploreState(State exploreState) throws PrismException {
		// TODO Auto-generated method stub
modGen.exploreState(exploreState);
	}

	@Override
	public int getNumChoices() throws PrismException {
		// TODO Auto-generated method stub
//		return 0;
		return modGen.getNumChoices();
	}

	@Override
	public int getNumTransitions(int i) throws PrismException {
		// TODO Auto-generated method stub
//		return 0;
		return modGen.getNumTransitions(i);
	}

	@Override
	public Object getTransitionAction(int i, int offset) throws PrismException {
		// TODO Auto-generated method stub
//		return null;
		return modGen.getTransitionAction(i, offset);
	}

	@Override
	public double getTransitionProbability(int i, int offset) throws PrismException {
		// TODO Auto-generated method stub
//		return 0;
		return modGen.getTransitionProbability(i, offset);
	}

	@Override
	public State computeTransitionTarget(int i, int offset) throws PrismException {
		// TODO Auto-generated method stub
//		return null;
		return modGen.computeTransitionTarget(i, offset);
	}

	@Override
	public ModelType getModelType() {
		// TODO Auto-generated method stub
//		return null;
		return modGen.getModelType();
	}

	@Override
	public List<String> getVarNames() {
		// TODO Auto-generated method stub
//		return null;
		return modGen.getVarNames();
	}

	@Override
	public List<Type> getVarTypes() {
		// TODO Auto-generated method stub
//		return null;
		return modGen.getVarTypes();
	}

	@Override
	public VarList createVarList() throws PrismException {
		// TODO Auto-generated method stub
//		return null;
		return modGen.createVarList();
	}

}
