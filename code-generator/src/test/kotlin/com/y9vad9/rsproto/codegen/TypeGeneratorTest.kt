package io.timemates.rsproto.codegen

import okio.FileSystem
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import kotlin.test.Test

class TypeGeneratorTest {
    @Test
    fun test() {
        val fileSystem = FakeFileSystem()

        fileSystem.write("test.proto".toPath(), mustCreate = true) {
            writeUtf8(
                """
            syntax = "proto3";
            package com.test;
            
            message Test {
                string test = 1;
                int32 test2 = 2;
                double test3 = 3;
                float test4 = 4;
                int64 test5 = 5;
                uint32 test6 = 6;
                uint64 test7 = 7;
                sint32 test8 = 8;
                sint64 test9 = 9;
                fixed32 test10 = 10;
                fixed64 test11 = 11;
                sfixed32 test12 = 12;
                sfixed64 test13 = 13;
                bool test14 = 14;
                bytes test15 = 15;
                string test16 = 16;
                repeated int32 test17 = 17;
            }
            
            message User {
                int32 id = 1;
                string name = 2;
                string bio = 3;
            }
            
            message Nothing {
                message Something {
                    int64 first = 1;
                }
            }
            
            enum EnumTesting {
                TEST = 0;
                TEST2 = 1;
            }
            
            service TestService {
                rpc getTest(Test) returns (Test);
                rpc getTestStream(Test) returns (stream Test);
                rpc getTestChannel(stream Test) returns (stream Test);
            }
            """.trimIndent()
            )
        }

        fileSystem.write("test2.proto".toPath(), mustCreate = true) {
            writeUtf8(
                """
            syntax = "proto3";
            package com.test.two;
            
            message Test {
                string test = 1;
                int32 test2 = 2;
                double test3 = 3;
                float test4 = 4;
                int64 test5 = 5;
                uint32 test6 = 6;
                uint64 test7 = 7;
                sint32 test8 = 8;
                sint64 test9 = 9;
                fixed32 test10 = 10;
                fixed64 test11 = 11;
                sfixed32 test12 = 12;
                sfixed64 test13 = 13;
                bool test14 = 14;
                bytes test15 = 15;
                string test16 = 16;
                repeated int32 test17 = 17;
            }
            
            message User {
                int32 id = 1;
                string name = 2;
                string bio = 3;
            }
            
            message Nothing {
                message Something {
                    int64 first = 1;
                }
            }
            
            enum EnumTesting {
                TEST = 0;
                TEST2 = 1;
            }
            
            service TestService {
                rpc getTest(Test) returns (Test);
                rpc getTestStream(Test) returns (stream Test);
                rpc getTestChannel(stream Test) returns (stream Test);
            }
            """.trimIndent()
            )
        }

        CodeGenerator(fileSystem).generate(
            rootPath = ".".toPath(),
            outputPath = "output".toPath(),
            clientGeneration = true,
            serverGeneration = true,
        )

        fileSystem.listRecursively("output".toPath()).forEach {
            fileSystem.read(it) {
                println(readString(Charsets.UTF_16))
            }
        }
    }

    @Test
    fun test2() {
        val fileSystem = FileSystem.SYSTEM
        val folder = "/Users/y9vad9/IdeaProjects/timemates/backend/infrastructure/rsocket-api/src/main/proto".toPath()

        CodeGenerator(fileSystem).generate(folder, "output".toPath(), true, true)
    }
}