namespace java being.altiplano.example.thriftmessage

struct EchoIn {
  1: string content
  2: i32 times
}

struct EchoOut {
  1: string reply
}

struct CountIn {
  1: string content
}

struct CountOut {
  1: i32 length
}

struct ReverseIn {
  1: string content
}

struct ReverseOut {
  1: string reply
}

struct UpperCastIn {
  1: string content
}

struct UpperCastOut {
  1: string reply
}

struct LowerCastIn {
  1: string content
}

struct LowerCastOut {
  1: string reply
}

