<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <artifactId>rpc-examples</artifactId>
        <groupId>being.altiplano</groupId>
        <version>1.0-SNAPSHOT</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <artifactId>protobuf-example</artifactId>
    <properties>
        <compiler-plugin.version>2.3.2</compiler-plugin.version>
        <protobuf.version>3.2.0</protobuf.version>
        <protobuf.input>${project.basedir}/src/main/proto</protobuf.input>
        <protobuf.output>${project.basedir}/src/main/proto-gen</protobuf.output>
    </properties>

    <dependencies>
        <dependency>
            <groupId>com.google.protobuf</groupId>
            <artifactId>protobuf-java</artifactId>
            <version>3.2.0</version>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>build-helper-maven-plugin</artifactId>
                <executions>
                    <execution>
                        <phase>generate-sources</phase>
                        <goals>
                            <goal>add-source</goal>
                        </goals>
                        <configuration>
                            <sources>
                                <source>src/main/proto</source>
                                <source>src/main/proto-gen</source>
                            </sources>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>${compiler-plugin.version}</version>
                <configuration>
                    <source>1.7</source>
                    <target>1.7</target>
                </configuration>
            </plugin>

            <plugin>
                <artifactId>maven-antrun-plugin</artifactId>
                <version>1.8</version>
                <executions>
                    <execution>
                        <id>compile-protobuf</id>
                        <phase>generate-sources</phase>
                        <configuration>
                            <tasks>
                                <mkdir dir="${protobuf.output}"/>
                                <echo message="Generate code from *.proto files:  ${protobuf.input}"/>

                                <!-- protoc -I=$SRC_DIR &#45;&#45;java_out=$DST_DIR $SRC_DIR/addressbook.proto -->
                                <apply executable="echo">
                                    <arg value="protoc"/>
                                    <arg value="-I=${protobuf.input}"/>
                                    <arg value="--proto_path=${protobuf.input}"/>
                                    <arg value="--java_out=${protobuf.output}"/>

                                    <fileset dir="${protobuf.input}" includes="*.proto"/>
                                </apply>

                                <apply executable="protoc">
                                    <arg value="-I=${protobuf.input}"/>
                                    <arg value="--java_out=${protobuf.output}"/>

                                    <fileset dir="${protobuf.input}" includes="*.proto"/>
                                </apply>
                            </tasks>
                        </configuration>
                        <goals>
                            <goal>run</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
            <plugin>
                <artifactId>maven-clean-plugin</artifactId>
                <configuration>
                    <verbose>true</verbose>
                    <filesets>
                        <fileset>
                            <directory>${project.basedir}/${protobuf.output}</directory>
                        </fileset>
                    </filesets>
                </configuration>
            </plugin>
        </plugins>
    </build>


</project>