
package Parse;

import java.util.ArrayList;
import Absyn.*;
import java.util.Optional;
import Parse.antlr_build.Parse.*;
import org.antlr.v4.runtime.ParserRuleContext;
import java.util.List;


/*
 * Hello, I assume that you have read the material in gParser.g4
 *
 * This file is your "Visitor". 
 *
 * Your job is to write visit functions for each parse rule in the gParser.g4 
 * file. Each visit function needs to return the corresponding Absyn node.
 *
 * The driver file you have been provided will print whatever is returned from
 * this visitor. If you successfully return the Absyn nodes, you will see them 
 * print in the terminal.
 *
 * If you get stuck of lost: Each context object can be found
 * in gParser.java. Just search "Context".
 *
 * 
*/

public class ASTBuilder extends gParserBaseVisitor<Absyn> {

   @Override
   public Absyn visitProgram(gParser.ProgramContext ctx) {
      DeclList decls = new DeclList(0);
      for (gParser.DeclarationContext dctx : ctx.declaration()) {
        decls.list.add((Decl)visit(dctx));
      }
      return decls;
   }

   //====================
   //    DECLARATIONS
   //====================

   @Override   //Variable Decleration
   public Absyn visitVarDecl(gParser.VarDeclContext ctx) {
      gParser.InitializationContext initalization = ctx.initialization();
      gParser.InitializerContext initializer = initalization.initializer();

      return new VarDecl(
              0,
              (Type)visit(ctx.type()),
              ctx.ID().getText(),
              (Exp)visit(initializer.expr())
     );
   }


   //===================
   //       TYPE
   //===================

   @Override   //Type
   public Absyn visitType(gParser.TypeContext ctx) {
      boolean constant = false;
      if(ctx.CONST()!=null){
         constant = true;
      }
      int numPointers = ctx.STAR().size();

      return new Type(
              0,
              constant,
              ctx.type_name().getText(),
              numPointers,
              new DeclList(0) //this still needs to be implemented using bracketlist
      );
   }


   //===================
   //    EXPRESSIONS
   //===================

   @Override   //Decimal Literal
   public Absyn visitDecLit(gParser.DecLitContext ctx){
      int value = Integer.parseInt(ctx.DECIMAL_LITERAL().getText());
      return new DecLit(0,value);
   }

   @Override   //String Literal
   public Absyn visitStrLit(gParser.StrLitContext ctx){
      String value = ctx.STRING_LITERAL().getText();
      return new StrLit(0,value);
   }

   @Override   //ID
   public Absyn visitID(gParser.IDContext ctx){
      String value = ctx.ID().getText();
      return new ID(0, value);
   }

   @Override   //BinaryOp
   public Absyn visitBinOp(gParser.BinOpContext ctx){
      Exp left = (Exp)visit(ctx.expr().get(0));
      String operator = ctx.op.getText();
      Exp right = (Exp)visit(ctx.expr().get(1));
      return new BinOp(
              0,
              left,
              operator,
              right
      );
   }

   @Override   //UnaryOp
   public Absyn visitUnaryExp(gParser.UnaryExpContext ctx){
      Exp exp = (Exp)visit(ctx.expr());
      String operator = ctx.unary_operator().getText();
      return new UnaryExp(
              0,
              operator,
              exp
      );
   }

   @Override   //Parenthesis Expression
   public Absyn visitParenExp(gParser.ParenExpContext ctx){
      return visit(ctx.expr());
   }

   @Override   //Array Expression
   public Absyn visitArrayExp(gParser.ArrayExpContext ctx){
      List<gParser.ExprContext> exprs = ctx.expr();
      Exp name = (Exp)visit(exprs.get(0));

      exprs.remove(0);
      ExpList indecies = new ExpList(exprs.size());
      for(gParser.ExprContext exp : exprs){
         indecies.list.add((Exp)visit(exp));
      }
      return new ArrayExp(
              0,
              name,
              indecies
      );
   }

   @Override   //Function Expression
   public Absyn visitFunExp(gParser.FunExpContext ctx){
      List<gParser.ExprContext> exprs = ctx.expr();
      Exp name = (Exp)visit(exprs.get(0));

      exprs.remove(0);
      ExpList parameters = new ExpList(exprs.size());
      for(gParser.ExprContext exp : exprs){
         parameters.list.add((Exp)visit(exp));
      }
      return new FunExp(
              0,
              name,
              parameters
      );
   }


   //===================
   //    STATEMENTS
   //===================

   @Override   //If Statement
   public Absyn visitIfStmt(gParser.IfStmtContext ctx) {
      return new IfStmt(
              0,
              (Exp)visit(ctx.expr()),
              (Stmt)visit(ctx.statement()),
              new EmptyStmt(0)
      );
   }
}

