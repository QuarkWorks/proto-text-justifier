package net.quarkworks.dev.proto_text_justifier

import net.sourceforge.argparse4j.ArgumentParsers
import net.sourceforge.argparse4j.impl.Arguments
import net.sourceforge.argparse4j.inf.ArgumentParser
import net.sourceforge.argparse4j.inf.ArgumentParserException
import net.sourceforge.argparse4j.inf.Namespace
import net.sourceforge.argparse4j.internal.HelpScreenException
import net.sourceforge.argparse4j.internal.UnrecognizedArgumentException
import net.sourceforge.argparse4j.internal.UnrecognizedCommandException
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.charset.Charset


fun main(args: Array<String>) {
    val parser = buildArgumentParser()
    val namespace: Namespace
    try {
        namespace = parser.parseArgs(args)

    } catch (e: ArgumentParserException) {
        myHandleError(e, parser)
        return
    }
    val file: File = namespace.get<File>("fileIn")
    val content = load(MyFileReader(file.path))
    val align = namespace.get<Alignment>("align")
    val width = namespace.get<Int>("width")
    println(
            myToString(AlignCoder(width, content.asSequence(), align))
    )

}

private fun buildArgumentParser(): ArgumentParser {
    val parser = ArgumentParsers
            .newArgumentParser("proto-text-justifier")
            .description("justifies input text")
    parser.addArgument("fileIn")
            .type(Arguments.fileType().verifyExists().verifyIsFile())
            .help("text file that will be white space split")
    parser.addArgument("--align")
            .type(Alignment::class.java)
            .help("text alignment")
            .default = Alignment.Justified
    parser.addArgument("width")
            .type(Int::class.java)
            .help("column width")
            .nargs("?")
            .default = 20
    parser.defaultHelp(true)

    return parser
}

private fun myHandleError(e: ArgumentParserException, parser: ArgumentParser) {
    when (e) {
        is HelpScreenException -> {
            parser.handleError(e)
        }
        is UnrecognizedArgumentException -> {
            parser.handleError(e)
            System.err.flush()
        }
        is UnrecognizedCommandException -> {
            parser.handleError(e)
            System.err.flush()
        }
        else -> {
            System.out.println(e.message)
            parser.printHelp()
            System.out.flush()
        }
    }
}

private fun load(fileReader: MyFileReader): List<String> {
    val `in` = Scanner(fileReader)
    val str_ar = generateSequence {`in`.yylex()}.toList()
    `in`.yyclose()
    return str_ar
}


fun myToString(`in`: AlignCoder): String {
    val sb = StringBuilder()
    for (code in `in`.markup) {
        when (code) {
            is ColEOL -> sb.append("|\n")
            is Content -> sb.append(code.v)
            is Space -> sb.append(code.v)
        }
    }
    return sb.toString()
}

class MyFileReader(path: String) : InputStreamReader(FileInputStream(path), Charset.forName("UTF-8"))
