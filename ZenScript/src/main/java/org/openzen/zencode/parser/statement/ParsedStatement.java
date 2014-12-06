/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zencode.parser.statement;

import org.openzen.zencode.symbolic.scope.IScopeMethod;
import org.openzen.zencode.symbolic.statement.Statement;
import org.openzen.zencode.symbolic.statement.StatementSwitch;
import org.openzen.zencode.lexer.Token;
import org.openzen.zencode.lexer.ZenLexer;
import static org.openzen.zencode.lexer.ZenLexer.*;
import org.openzen.zencode.symbolic.expression.IPartialExpression;
import org.openzen.zencode.symbolic.type.IZenType;
import org.openzen.zencode.util.CodePosition;

/**
 *
 * @author Stan
 */
public abstract class ParsedStatement
{
	public static ParsedStatement parse(ZenLexer lexer)
	{
		Token next = lexer.peek();
		switch (next.getType()) {
			case T_AOPEN:
				return ParsedStatementBlock.parse(lexer);
				
			case T_IMPORT:
				return ParsedImportStatement.parse(lexer);

			case T_RETURN:
				return ParsedStatementReturn.parse(lexer);

			case T_VAR:
			case T_VAL:
				return ParsedStatementVar.parse(lexer);

			case T_IF:
				return ParsedStatementIf.parse(lexer);

			case T_FOR:
				return ParsedStatementFor.parse(lexer);

			case T_SWITCH:
				return ParsedStatementSwitch.parse(lexer);

			case T_CASE:
				return ParsedStatementCase.parse(lexer);

			case T_DEFAULT:
				return ParsedStatementDefault.parse(lexer);

			case T_BREAK:
				return ParsedStatementBreak.parse(lexer);

			case T_CONTINUE:
				return ParsedStatementContinue.parse(lexer);

			case T_WHILE:
				return ParsedStatementWhile.parse(lexer);

			case T_DO:
				return ParsedStatementDoWhile.parse(lexer);

			default:
				return ParsedStatementExpression.parse(lexer);
		}
	}

	private final CodePosition position;

	public ParsedStatement(CodePosition position)
	{
		this.position = position;
	}

	public CodePosition getPosition()
	{
		return position;
	}

	public abstract <E extends IPartialExpression<E, T>, T extends IZenType<E, T>>
		 Statement<E, T> compile(IScopeMethod<E, T> scope);

	public abstract <E extends IPartialExpression<E, T>, T extends IZenType<E, T>>
		 void compileSwitch(IScopeMethod<E, T> scope, StatementSwitch<E, T> forSwitch);
}