package Parse;

import Absyn.*;
import Parse.antlr_build.Parse.*;

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
         decls.list.add((Decl) visit(dctx));
      }
      return decls;
   }

   //====================
   //    DECLARATIONS
   //====================

   @Override   //Variable Declaration
   public Absyn visitVarDecl(gParser.VarDeclContext ctx) {
      gParser.InitializationContext initialization = ctx.initialization();
      gParser.InitializerContext initializer = initialization.initializer();
      Exp init = (initializer != null) ? (Exp) visit(initializer) : new EmptyExp(0);

      return new VarDecl(
              0,
              (Type) visit(ctx.type()),
              ctx.ID().getText(),
              init
      );
   }

   @Override   //Struct or Union Declaration
   public Absyn visitStructOrUnionDecl(gParser.StructOrUnionDeclContext ctx) {
      String name = ctx.ID(0).getText();
      DeclList body = new DeclList(0);
      for (int i = 0; i < ctx.type().size(); i++) {

         if (ctx.STRUCT() != null) {
            body.list.add(new StructMember(0, (Type) visit(ctx.type(i)), ctx.ID(i + 1).getText()));
         } else {
            body.list.add(new UnionMember(0, (Type) visit(ctx.type(i)), ctx.ID(i + 1).getText()));
         }

      }
      if (ctx.STRUCT() != null) {
         return new StructDecl(0, name, body);
      } else {
         return new UnionDecl(0, name, body);
      }
   }

   @Override   //Function Declaration
   public Absyn visitFunDecl(gParser.FunDeclContext ctx) {
      DeclList params = (ctx.parameters() != null) ? (DeclList) visit(ctx.parameters()) : new DeclList(0);
      Stmt body = (Stmt) visit(ctx.statement());
      if (body == null) body = new EmptyStmt(0);
      return new FunDecl(
              0,
              (Type) visit(ctx.type()),
              ctx.ID().getText(),
              params,
              body
      );
   }

   @Override   //Typedef Declaration
   public Absyn visitTypedefDecl(gParser.TypedefDeclContext ctx) {
      return new Typedef(0, (Type) visit(ctx.type()), ctx.ID().getText());
   }

   //====================
   //    PARAMETERS
   //====================

   @Override   //Parameters (type ID (COMMA type ID)*)
   public Absyn visitParameters(gParser.ParametersContext ctx) {
      DeclList params = new DeclList(0);
      for (int i = 0; i < ctx.type().size(); i++) {
         params.list.add(new Parameter(0, (Type) visit(ctx.type(i)), ctx.ID(i).getText()));
      }
      return params;
   }


   //===================
   //       TYPE
   //===================

   Override   //Type

   public Absyn visitType(gParser.TypeContext ctx) {
      boolean constant = false;
      if (ctx.CONST() != null) {
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

   //visitType_name
   @Override
   public Absyn visitType_name(gParser.Type_nameContext ctx) {
      return new Type(0, false, ctx.getText(), 0, new DeclList(0));
   }


   //===================
   //    EXPRESSIONS
   //===================

   @Override   //Decimal Literal
   public Absyn visitDecLit(gParser.DecLitContext ctx) {
      int value = Integer.parseInt(ctx.DECIMAL_LITERAL().getText());
      return new DecLit(0, value);
   }

   @Override   //String Literal
   public Absyn visitStrLit(gParser.StrLitContext ctx) {
      String value = ctx.STRING_LITERAL().getText();
      return new StrLit(0, value);
   }

   @Override   //ID
   public Absyn visitID(gParser.IDContext ctx) {
      String value = ctx.ID().getText();
      return new ID(0, value);
   }

   @Override   //BinaryOp
   public Absyn visitBinOp(gParser.BinOpContext ctx) {
      Exp left = (Exp) visit(ctx.expr().get(0));
      String operator = ctx.op.getText();
      Exp right = (Exp) visit(ctx.expr().get(1));
      return new BinOp(
              0,
              left,
              operator,
              right
      );
   }

   @Override   //UnaryOp
   public Absyn visitUnaryExp(gParser.UnaryExpContext ctx) {
      Exp exp = (Exp) visit(ctx.expr());
      String operator = ctx.unary_operator().getText();
      return new UnaryExp(
              0,
              operator,
              exp
      );
   }

   @Override   //Parenthesis Expression
   public Absyn visitParenExp(gParser.ParenExpContext ctx) {
      return visit(ctx.expr());
   }

   @Override   //Array Expression
   public Absyn visitArrayExp(gParser.ArrayExpContext ctx) {
      List<gParser.ExprContext> exprs = ctx.expr();
      Exp name = (Exp) visit(exprs.get(0));

      exprs.remove(0);
      ExpList indecies = new ExpList(exprs.size());
      for (gParser.ExprContext exp : exprs) {
         indecies.list.add((Exp) visit(exp));
      }
      return new ArrayExp(
              0,
              name,
              indecies
      );
   }

   @Override   //Function Expression
   public Absyn visitFunExp(gParser.FunExpContext ctx) {
      List<gParser.ExprContext> exprs = ctx.expr();
      Exp name = (Exp) visit(exprs.get(0));

      exprs.remove(0);
      ExpList parameters = new ExpList(exprs.size());
      for (gParser.ExprContext exp : exprs) {
         parameters.list.add((Exp) visit(exp));
      }
      return new FunExp(
              0,
              name,
              parameters
      );
   }

   //visitAssignExp
   @Override
   public Absyn visitAssignExp(gParser.AssignExpContext ctx) {
      Exp left = (Exp) visit(ctx.getChild(0));
      Exp right = (Exp) visit(ctx.getChild(2));
      return new BinOp(0, left, "=", right);
   }


   //===================
   //    STATEMENTS
   //===================

   @Override // Compound Statement
   public Absyn visitCompStmt(gParser.CompStmtContext ctx) {
      DeclList decls = new DeclList(0);
      for (int i = 0; i < ctx.declaration().size(); i++) {
         decls.list.add((Decl) visit(ctx.declaration().get(i)));
      }

      StmtList stmts = new StmtList(0);
      for (int i = 0; i < ctx.statement().size(); i++) {
         stmts.list.add((Stmt) visit(ctx.statement().get(i)));
      }

      return new CompStmt(0, decls, stmts);
   }

   @Override   //If Statement
   public Absyn visitIfStmt(gParser.IfStmtContext ctx) {
      return new IfStmt(
              0,
              (Exp) visit(ctx.expr()),
              (Stmt) visit(ctx.statement()),
              new EmptyStmt(0)
      );
   }

   @Override // If Else Statement
   public Absyn visitIfElseStmt(gParser.IfElseStmtContext ctx) {
      return new IfStmt(
              0,
              (Exp) visit(ctx.expr()),
              (Stmt) visit(ctx.statement(0)),
              (Stmt) visit(ctx.statement(1))
      );
   }

   @Override // While Statement
   public Absyn visitWhileStmt(gParser.WhileStmtContext ctx) {
      return new WhileStmt(
              0,
              (Exp) visit(ctx.expr()),
              (Stmt) visit(ctx.statement())
      );
   }

   @Override //Exp Stmt
   public Absyn visitExprStmt(gParser.ExprStmtContext ctx) {
      return new ExprStmt(
              0, (Exp) visit(ctx.expr())
      );
   }

   @Override //Return statement
   public Absyn visitReturnStmt(gParser.ReturnStmtContext ctx) {
      return new ReturnStmt(
              0,
              (Exp) visit(ctx.initializer())
      );
   }

   @Override //Break
   public Absyn visitBreakStmt(gParser.BreakStmtContext ctx) {
      return new BreakStmt(0);
   }

   //====================
   //  INITIALIZATION
   //====================
   //visitInitialization
   @Override
   public Absyn visitInitialization(gParser.InitializationContext ctx) {


      if (ctx.initializer() != null) {
         return visit(ctx.initializer());
      } else {
         return new EmptyExp(0);
      }
   }

   //====================
   //   INITIALIZER
   //====================
   @Override   //Initializer (expr or LCURLY initializer (COMMA initializer)* RCURLY)
   public Absyn visitInitializer(gParser.InitializerContext ctx) {

      if (ctx.LCURLY() != null) {
         ExpList list = new ExpList(0);
         for (int i = 0; i < ctx.initializer().size(); i++) {
            list.list.add((Exp) visit(ctx.initializer(i)));
         }
         return list;
      } else {
         return visit(ctx.expr());
      }
   }

   //====================
   //     BRACKETS
   //====================
   //visitExprArrayBracket
   @Override
   public Absyn visitExprArrayBrackets(gParser.ExprArrayBracketsContext ctx)
   {
      Exp index = (Exp) visit(ctx.expr(0));
      return index;
   }

   //visitEmptyArrayBrackets
   @Override
   public Absyn visitEmptyArrayBrackets(gParser.EmptyArrayBracketsContext ctx)
   {return new EmptyExp(0);}


}



