package net.quarkworks.dev.proto_text_justifier

val WS = Regex("[ \t\u000c\r\n\u000b\u0085\u00A0]+")

fun myAssert(b:Boolean, msg:String)
{
    if (!b){
        throw RuntimeException("assert fail: $msg")
    }
}