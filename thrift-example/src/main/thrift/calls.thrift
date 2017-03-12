include "messages.thrift"

namespace java being.altiplano.example.thriftrpc

service Asker {
  messages.EchoOut echo(1: messages.EchoIn param),
  messages.CountOut count(1: messages.CountIn param),
  messages.ReverseOut reverse(1: messages.ReverseIn param),
  messages.UpperCastOut uppercast(1: messages.UpperCastIn param),
  messages.LowerCastOut lowercast(1: messages.LowerCastIn param)
}