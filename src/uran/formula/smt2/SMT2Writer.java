/* +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 * +++++++++++++++++++++++++++++++++++Written by: Hao Wu++++++++++++++++++++++++++++++++
 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 *
 *	This is a part of my research work.
 *  haowu@cs.nuim.ie
 *  JULY-2015 
 *  
 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 * ++++++++++++++++++++++++++++++Do or do not, there is no try.+++++++++++++++++++++++++
 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 */

package uran.formula.smt2;

import uran.formula.visitor.AbstractVisitor;
import uran.formula.AbstractFormula;
import uran.formula.ArithmeticFormula;
import uran.formula.AndFormula;
import uran.formula.OrFormula;
import uran.formula.OneFormula;
import uran.formula.ImpliesFormula;
import uran.formula.Function;
import uran.formula.BinaryFormula;
import uran.formula.QuantifiedFormula;
import uran.formula.Constant;
import uran.formula.Variable;
import uran.formula.Literal;
import uran.formula.BoolLiteral;
import uran.formula.NumLiteral;
import uran.formula.Decls;
import uran.formula.EqFormula;
import uran.formula.NegFormula;
import uran.formula.ComparisonFormula;
import uran.formula.Scope;
import uran.formula.Connective;
import uran.formula.FunctionFactory;
import uran.err.NullableFormulaException;
import uran.err.MissFileNameException;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.util.List;
import java.util.Date;

public final class SMT2Writer extends AbstractVisitor implements Runnable{
	private String file="";
	private PrintWriter writer;	
	private RandomAccessFile raf;
	private FunctionFactory factory;
	private List<AbstractFormula> formulas;
	private final static String ASSERT="assert";
	
	/* disable the default constructor to prevent it from creating a null writer */
	private SMT2Writer(){;}

	public SMT2Writer (String file, FunctionFactory factory, List<AbstractFormula> formulas){
		if (factory==null) throw new NullableFormulaException("Error: factory is null.");
		if (formulas==null) throw new NullableFormulaException("Error: formula cannot be null.");
		if (file==null) throw new NullableFormulaException("Error: file cannot be null.");
		if (file=="") throw new MissFileNameException("Error: file name is empty.");
		
		this.file=file;
		this.factory=factory;
		this.formulas =formulas;
		try{
			writer = new PrintWriter (new BufferedWriter (new FileWriter(this.file)));
			generate();
			writer.flush();
			writer.close();
		}
		catch (IOException e){
			System.err.println("IO Error:"+e.getMessage());
		}
	}
	
	public void visit (QuantifiedFormula f){assemble(f);}
	public void visit (NegFormula f){assemble(f);}
	public void visit (Function f){assemble(f);}
	public void visit (BinaryFormula f){assemble(f);}
	public void visit (Scope s){assemble(s);}
	public void visit (ImpliesFormula f){assemble(f);}
	public void visit (BoolLiteral l){assemble(l);}
	public void visit (NumLiteral l){assemble(l);}
	//public void visit (AppliedFunction f){assemble(f);}
	public void visit (Decls d){; /* do nothing */ }
	public void run(){
		
	}
	
	private void assemble(AbstractFormula formula){
		writer.println("("+ASSERT+" "+formula.toSMT2()+ ")");
	}
	
	public void assemble(Scope s){
		while (s!=null){
			writer.println("(push)");
			for (AbstractFormula f: s.getContext()) assemble(f);
			writer.println("(pop)");
			s=s.getScope();
		}
	}

	private synchronized void generate(){
		/* generate all declaration */
		List<Function>	decls = factory.getAllFunctions();
		writer.println("\n;this file is automatically generated: "+new Date());
		writer.println("\n;declarations generated\n");
		for (int i=0;i<decls.size();i++)
			writer.write("(declare-fun "+decls.get(i).toSMT2_decl()+" )\n");

		writer.write("\n;formulas generated\n");
		for (int i=0;i<formulas.size();i++) formulas.get(i).accept(this);
		writer.write("\n ;end of formula ");
		
	}
	public FunctionFactory getFactory(){return this.factory;}
	public String getFile(){return this.file;}

	public synchronized void append(AbstractFormula formula){
		try{
			raf = new RandomAccessFile(this.file,"rw");
			raf.seek(raf.length());
			raf.writeBytes("\n");
			raf.writeBytes("("+ASSERT+" "+formula.toSMT2()+")\n");
			raf.close();
		}
		catch (IOException e){throw new RuntimeException("Error: failed to add on new formulas: "+file);}			

	}

	public synchronized void append(List<AbstractFormula> formulas){
		try{
			raf = new RandomAccessFile(this.file,"rw");
			raf.seek(raf.length());
			raf.writeBytes("\n");
			for (AbstractFormula formula : formulas)
				raf.writeBytes("("+ASSERT+" "+formula.toSMT2()+")\n");
			raf.close();
		}
		catch (IOException e){throw new RuntimeException("Error: failed to add on new formulas: "+file);}	
	}

	public synchronized void overwrite(List<AbstractFormula> formulas, int line){
		try{
			raf = new RandomAccessFile(this.file,"rw");
			long pos = raf.length()-1;
			raf.seek(pos);
			int k=0;
			// discard the last newline
			if ((char)raf.read()=='\n') pos--;
			for (;pos>=0; --pos){
				raf.seek(pos);
				if (k>=line) break;
				if ((char)raf.read()=='\n') k++;	
			}
			raf.setLength(pos);
			raf.writeBytes("\n");
			for (AbstractFormula formula : formulas)
				raf.writeBytes("("+ASSERT+" "+formula.toSMT2()+")\n");

			raf.close();
		}
		catch (IOException e){throw new RuntimeException("Error: failed to overwrite formulas: "+file+" "+e.getMessage());}	
	}
	
}