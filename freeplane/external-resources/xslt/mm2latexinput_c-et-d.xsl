<?xml version="1.0"?>

	<!--
		MINDMAPEXPORTFILTER tex  latex input with cites and details
		
		: This code	released under the GPL. 
		: (http://www.gnu.org/copyleft/gpl.html)
		
		Document : mm2latexinput_c-et-d.xsl
		changes by stefan.theurich@tu-dresden.de
		
		based on
		Document : mm2latexinput.xsl
		Created on : 17 June 2013
		Author : joerg feuerhake joerg.feuerhake@free-penguin.org 
		Description: transforms freeplane mm format to latex scrartcl, 
		handles crossrefs ignores the rest. 
		feel free to customize it while leaving the ancient
		authors mentioned. thank you 
		Thanks to: Tayeb.Lemlouma@inrialpes.fr	for writing the LaTeX escape scripts and giving inspiration 
		
		ChangeLog:	See: http://freeplane.sourceforge.net/
	-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0">
	<xsl:output omit-xml-declaration="yes"  method="text"/>

	<!-- BEGIN OPTION XSLT 2 / extensions -->
	<!-- this should be called as follows (user selected base file name with or without .tex [e.g. content] ) -->
	<!-- saxonb-xslt -w1 -ext:on -s:<mindmap.mm> -xsl:mm2atexinput.xsl output-base=<filename> -->
	<xsl:param name="output-base" select="''" />
	
	<xsl:template match="map">
		<xsl:variable name="filename">
			<xsl:choose>
				<!-- parameter given with trailing .tex -->
				<xsl:when test="($output-base != '') and (substring($output-base, string-length($output-base)-3, 4) = '.tex')">
					<xsl:value-of select="substring($output-base, 1, string-length($output-base)-4)" />
				</xsl:when>
				<!-- parameter given -->
				<xsl:when test="$output-base != ''">
					<xsl:value-of select="$output-base" />
				</xsl:when>
				<!-- else -->
				<xsl:otherwise>content</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
			
		<xsl:result-document method="text" href="{$filename}.tex">
			<!-- recursively process all subnodes of the root node -->
			<xsl:for-each select="node/node">
				<xsl:variable name="id" select="replace(concat(position(),'-',substring(@TEXT, 1, 5)), ' ', '_')" />
				<xsl:text>\input{</xsl:text>
				<xsl:value-of select="$filename" />
				<xsl:text>_</xsl:text>
				<xsl:value-of select="$id" />
				<xsl:text>}
</xsl:text>
				<xsl:result-document method="text" href="{$filename}_{$id}.tex">
					<xsl:apply-templates select="." />
				</xsl:result-document>
			</xsl:for-each>
		</xsl:result-document>
	</xsl:template>
	<!-- BEGIN OPTION XSLT 2 / extensions -->
	
	<!-- BEGIN OPTION XSLT 1 / no extensions -->
	<!-- this should be called as follows (user selected file name with .tex [e.g. content] ) -->
	<!-- saxonb-xslt -w1 -s:<mindmap.mm> -xsl:mm2atexinput.xsl -o:<filename.tex> -->
	<!--
	<xsl:template match="map">
		<xsl:apply-templates select="node/node" />
	</xsl:template>
	-->
	<!-- END OPTION XSLT 1 / no extensions -->


	<!-- ======= Body ====== -->

	<!-- Sections Processing -->
	<xsl:template match="node">
		<!-- <xsl:variable name="target" select="arrowlink/@DESTINATION"/> -->
		
		<xsl:choose>
			<xsl:when test="hook[@NAME = 'ExternalObject']">
				<xsl:text>\begin{figure}[htb]
 \begin{center}
  \includegraphics[width=12cm]{</xsl:text>
				<xsl:value-of select="substring-before(hook/@URI, '.png')"/>
				<xsl:text>}
  \caption{</xsl:text>
				<xsl:apply-templates select="@TEXT|richcontent" mode="trim" />
				<xsl:text>}
</xsl:text>
				<xsl:apply-templates select="richcontent[@TYPE = 'NOTE']" mode="todo" />
				<xsl:text> \end{center}
\end{figure}</xsl:text>
			</xsl:when>
			
			<xsl:when test="(@NUMBERED = 'true') and (count(ancestor::node())-2 &lt;= 1)">
				<xsl:text>\chapter{</xsl:text>
				<xsl:apply-templates select="@TEXT" mode="trim" />
				<xsl:text>}
</xsl:text>
				<xsl:apply-templates select="richcontent[@TYPE = 'NOTE']" mode="todo" />
				
				<xsl:apply-templates select="richcontent[@TYPE = 'DETAILS']" />

				<xsl:apply-templates select="node" />
				
				<xsl:text>% END chapter{</xsl:text>
				<xsl:apply-templates select="@TEXT" mode="trim"/>
				<xsl:text>}

</xsl:text>
			</xsl:when>
			
			<xsl:when test="(@NUMBERED = 'true') and (count(ancestor::node())-2 = 2)">
				<xsl:text>\section{</xsl:text>
				<xsl:apply-templates select="@TEXT" mode="trim" />
				<xsl:text>}
</xsl:text>
				<xsl:apply-templates select="richcontent[@TYPE = 'NOTE']" mode="todo" />
				
				<xsl:apply-templates select="richcontent[@TYPE = 'DETAILS']" />
				
				<xsl:apply-templates select="node" />
				
				<xsl:text>% END section{</xsl:text>
				<xsl:apply-templates select="@TEXT" mode="trim" />
				<xsl:text>}

</xsl:text>
			</xsl:when>
			
			<xsl:when test="(@NUMBERED = 'true') and (count(ancestor::node())-2 = 3)">
				<xsl:text>\subsection{</xsl:text>
				<xsl:apply-templates select="@TEXT" mode="trim" />
				<xsl:text>}
</xsl:text>
				<xsl:apply-templates select="richcontent[@TYPE = 'NOTE']" mode="todo" />
				
				<xsl:apply-templates select="richcontent[@TYPE = 'DETAILS']" />
				
				<xsl:apply-templates select="node" />
				
				<xsl:text>% END subsection{</xsl:text>
				<xsl:apply-templates select="@TEXT" mode="trim" />
				<xsl:text>}

</xsl:text>
			</xsl:when>
			
			<xsl:when test="(@NUMBERED = 'true') and (count(ancestor::node())-2 = 4)">
				<xsl:text>\subsubsection{</xsl:text>
				<xsl:apply-templates select="@TEXT" mode="trim" />
				<xsl:text>}
</xsl:text>
				<xsl:apply-templates select="richcontent[@TYPE = 'NOTE']" mode="todo" />
				
				<xsl:apply-templates select="richcontent[@TYPE = 'DETAILS']" />
				
				<xsl:apply-templates select="node" />
				
				<xsl:text>% END subsubsection{</xsl:text>
				<xsl:apply-templates select="@TEXT" mode="trim" />
				<xsl:text>}

</xsl:text>
			</xsl:when>
			
			<!-- citation -->
			<xsl:when test="attribute[@NAME = 'key']">
				<xsl:text>

\textbf{ \cite{</xsl:text>
				<xsl:value-of select="attribute[@NAME = 'key']/@VALUE" />
				<xsl:text>} (</xsl:text>
				<xsl:apply-templates select="@TEXT" mode="trim" />
				<xsl:text>) }</xsl:text>
				
				<xsl:choose>
					<xsl:when test="node">
						<xsl:text> details:
</xsl:text>
						<xsl:apply-templates select="richcontent[@TYPE = 'NOTE']" mode="todo" />
						<xsl:text>
\begin{description}
</xsl:text>
						<xsl:apply-templates select="node" mode="description">
							<xsl:with-param name="indent" select="'  '" />
						</xsl:apply-templates>
						
						<xsl:text>\end{description} % END {</xsl:text>
						<xsl:value-of select="attribute[@NAME = 'key']/@VALUE" />
						<xsl:text>}

</xsl:text>
					</xsl:when>
					<xsl:otherwise>
						<xsl:text>
</xsl:text>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			
			<!-- unnumbered node has subnodes -->
			<xsl:when test="node">
				<xsl:text>

\textbf{ </xsl:text>
				<xsl:apply-templates select="@TEXT" mode="trim" />
				<xsl:text>}
</xsl:text>
				<xsl:apply-templates select="richcontent[@TYPE = 'NOTE']" mode="todo" />
				
				<xsl:apply-templates select="richcontent[@TYPE = 'DETAILS']" />
				
				<xsl:text>\begin{description}
</xsl:text>
				<xsl:apply-templates select="node" mode="description">
					<xsl:with-param name="indent" select="'  '" />
				</xsl:apply-templates>
						
				<xsl:text>\end{description} % END {</xsl:text>
				<xsl:apply-templates select="@TEXT" mode="trim" />
				<xsl:text>}

</xsl:text>
			</xsl:when>
			
			<!-- empty text, but detail -->
			<xsl:when test="@TEXT = ''">
				<!-- write text, ignore layer -->
				<xsl:apply-templates select="richcontent[@TYPE = 'NOTE']" mode="todo" />
				
				<xsl:apply-templates select="richcontent[@TYPE = 'DETAILS']" />
				<xsl:apply-templates select="node" />
			</xsl:when>
			
			<xsl:otherwise>
				<xsl:choose>
					<xsl:when test="starts-with(@TEXT, '\latex ')">
						<xsl:value-of select="substring-after(@TEXT, '\latex ')"/>
					</xsl:when>
					
					<xsl:when test="@FORMAT = 'latexPatternFormat'">
						<xsl:apply-templates select="@TEXT|richcontent[@TYPE = 'DETAILS']"  mode="rawLatex"/>
					</xsl:when>
					
					<xsl:otherwise>
						<xsl:apply-templates select="@TEXT|richcontent[@TYPE = 'DETAILS']"  mode="addEol"/>
					</xsl:otherwise>
				</xsl:choose>
				
				<xsl:choose>
					<xsl:when test="node">
						<xsl:apply-templates select="richcontent[@TYPE = 'NOTE']" mode="todo" />
				
						<xsl:text>% BEGIN {</xsl:text>
						<xsl:apply-templates select="@TEXT" mode="trim"/>
						<xsl:text>}
</xsl:text>
						<xsl:text>\begin{description}
</xsl:text>
						<xsl:apply-templates select="node" mode="description">
							<xsl:with-param name="indent"  select="'  '" />
						</xsl:apply-templates>
						<xsl:text>\end{description} % END {</xsl:text>
						<xsl:apply-templates select="@TEXT" mode="trim"/>
						<xsl:text>}
</xsl:text>
					</xsl:when>
				</xsl:choose>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="node" mode="description">
		<xsl:param name="indent" />
		<!-- <xsl:variable name="target" select="arrowlink/@DESTINATION"/> -->
		
		<xsl:choose>
			<xsl:when test="hook[@NAME = 'ExternalObject']">
				<xsl:value-of select="$indent" />
				<xsl:text>\begin{figure}[htb]
 </xsl:text>
 				<xsl:value-of select="$indent" />
 				<xsl:text>\begin{center}
 </xsl:text>
 				<xsl:value-of select="$indent" />
 				<xsl:text> \includegraphics[width=12cm]{</xsl:text>
				<xsl:value-of select="substring-before(hook/@URI, '.png')"/>
				<xsl:text>}
</xsl:text>
				<xsl:value-of select="$indent" />
				<xsl:text>  \caption{</xsl:text>
				<xsl:apply-templates select="@TEXT|richcontent[@TYPE = 'DETAILS']" mode="trim" />
				<xsl:text>}
</xsl:text>
				<xsl:apply-templates select="richcontent[@TYPE = 'NOTE']" mode="todo" />
				
				<xsl:value-of select="$indent" />
				<xsl:text> \end{center}
</xsl:text>
				<xsl:value-of select="$indent" />
				<xsl:text>\end{figure}
</xsl:text>
			</xsl:when>
			
			<!-- citation -->
			<xsl:when test="attribute[@NAME = 'key']">
				<xsl:value-of select="$indent" />
				<xsl:text>\item[ \cite{</xsl:text>
				<xsl:value-of select="attribute[@NAME = 'key']/@VALUE" />
				<xsl:text>} ] (</xsl:text>
				<xsl:apply-templates select="@TEXT" mode="trim" />
				<xsl:text>)</xsl:text>
				
				<xsl:choose>
					<xsl:when test="node">
						<xsl:text> details:
</xsl:text>
						<xsl:apply-templates select="richcontent[@TYPE = 'NOTE']" mode="todo" />
				
						<xsl:value-of select="$indent" />
						<xsl:text>\begin{description}
</xsl:text>
						<xsl:apply-templates select="node" mode="description">
							<xsl:with-param name="indent" select="concat($indent, '  ')" />
						</xsl:apply-templates>
						
						<xsl:value-of select="$indent" />
						<xsl:text>\end{description} % END cite{</xsl:text>
						<xsl:value-of select="attribute[@NAME = 'key']/@VALUE" />
						<xsl:text>}

</xsl:text>
					</xsl:when>
					<xsl:otherwise>
						<xsl:text>
</xsl:text>
						<xsl:apply-templates select="richcontent[@TYPE = 'NOTE']" mode="todo" />
				
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			
			<!-- empty text, but detail -->
			<xsl:when test="@TEXT = ''">
				<xsl:value-of select="$indent" />
				<xsl:text>\item[--empty node--] - </xsl:text>
				<xsl:apply-templates select="richcontent[@TYPE = 'DETAILS']" mode="trim" />
				<xsl:text>
</xsl:text>
				<xsl:apply-templates select="richcontent[@TYPE = 'NOTE']" mode="todo" />
				
				<xsl:apply-templates select="node" mode="description">
					<xsl:with-param name="indent" select="concat($indent, '  ')" />
				</xsl:apply-templates>
			</xsl:when>
			
			<xsl:otherwise>
				<xsl:choose>
					<xsl:when test="starts-with(@TEXT, '\latex ')">
						<xsl:value-of select="substring-after(@TEXT, '\latex ')"/>
					</xsl:when>
					
					<xsl:when test="@FORMAT = 'latexPatternFormat'">
						<xsl:apply-templates select="@TEXT|richcontent[@TYPE = 'DETAILS']"  mode="rawLatex"/>
					</xsl:when>
					
					<xsl:otherwise>
						<xsl:value-of select="$indent" />
						<xsl:text>\item[</xsl:text>
						<xsl:apply-templates select="@TEXT" mode="trim"/>
						<xsl:text>] - </xsl:text>
						<xsl:apply-templates select="richcontent[@TYPE = 'DETAILS']" mode="trim" />
						<xsl:text>
</xsl:text>
					</xsl:otherwise>
				</xsl:choose>
				
				<xsl:apply-templates select="richcontent[@TYPE = 'NOTE']" mode="todo" />
				
				<xsl:choose>
					<xsl:when test="node">
						<xsl:value-of select="$indent" />
						<xsl:text>% BEGIN {</xsl:text>
						<xsl:apply-templates select="@TEXT" mode="trim"/>
						<xsl:text>}
</xsl:text>
						<xsl:value-of select="$indent" />
						<xsl:text>\begin{description}
</xsl:text>
						<xsl:apply-templates select="node" mode="description">
							<xsl:with-param name="indent" select="concat($indent, '  ')" />
						</xsl:apply-templates>
						<xsl:value-of select="$indent" />
						<xsl:text>\end{description} % END {</xsl:text>
						<xsl:apply-templates select="@TEXT" mode="trim"/>
						<xsl:text>}
</xsl:text>
					</xsl:when>
				</xsl:choose>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- End of Sections Processing -->

	<!--Text Process -->
	<!--<xsl:apply-templates select="Body/node()"/>-->


	<xsl:template match="richcontent[@TYPE = 'NOTE']" mode="todo">
		<xsl:text>\todo{</xsl:text>
			<xsl:apply-templates select="." mode="trim" />
		<xsl:text>}

</xsl:text>
	</xsl:template>

	<xsl:template match="richcontent"  >
		<xsl:apply-templates select="html"/>
	</xsl:template>

	<xsl:template match="@TEXT" mode="addEol">
		<xsl:apply-templates select="."/>
		<xsl:text>

</xsl:text>
	</xsl:template>

	<xsl:template match="richcontent" mode="addEol">
		<xsl:apply-templates select="."/>
		<xsl:text>

</xsl:text>
	</xsl:template>

	<xsl:template match="@TEXT" mode="trim">
		<xsl:variable name="text">
			<xsl:apply-templates select="."/>
		</xsl:variable>
		<xsl:value-of select="normalize-space($text)" />
	</xsl:template>

	<xsl:template match="richcontent" mode="trim">
		<xsl:variable name="text">
			<xsl:apply-templates select="."/>
		</xsl:variable>
		<xsl:value-of select="normalize-space($text)" />
	</xsl:template>

	<!-- LaTeXChar: A recursive function that generates LaTeX special characters -->
	<xsl:template match = "@*|text()" mode="rawLatex">
		<xsl:value-of select="."/>
	</xsl:template>

	<xsl:template match = "@*|text()">
		<xsl:call-template name="esc">
			<xsl:with-param name="c" select="'&#160;'"/>
			<xsl:with-param name="s">
				<xsl:call-template name="esc">
					<xsl:with-param name="c" select="'#'"/>
					<xsl:with-param name="s">
						<xsl:call-template name="esc">
							<xsl:with-param name="c" select="'$'"/>
							<xsl:with-param name="s">
								<xsl:call-template name="esc">
									<xsl:with-param name="c" select="'%'"/>
									<xsl:with-param name="s">
										<xsl:call-template name="esc">
											<xsl:with-param name="c" select="'&amp;'"/>
											<xsl:with-param name="s">
												<xsl:call-template name="esc">
													<xsl:with-param name="c" select="'~'"/>
													<xsl:with-param name="s">
														<xsl:call-template name="esc">
															<xsl:with-param name="c" select="'_'"/>
															<xsl:with-param name="s">
																<xsl:call-template name="esc">
																	<xsl:with-param name="c" select="'^'"/>
																	<xsl:with-param name="s">
																		<xsl:call-template name="esc">
																			<xsl:with-param name="c" select="'{'"/>
																			<xsl:with-param name="s">
																				<xsl:call-template name="esc">
																					<xsl:with-param name="c" select="'}'"/>
																					<xsl:with-param name="s">
																						<xsl:call-template name="esc">
																							<xsl:with-param name="c" select="'&quot;'"/>
																							<xsl:with-param name="s">
																								<xsl:call-template name="esc">
																									<xsl:with-param name="c" select="'\'"/>
																									<xsl:with-param name="s">
																										<xsl:value-of select="normalize-space(.)"/>
																									</xsl:with-param>
																								</xsl:call-template>
																							</xsl:with-param>
																						</xsl:call-template>
																					</xsl:with-param>
																				</xsl:call-template>
																			</xsl:with-param>
																		</xsl:call-template>
																	</xsl:with-param>
																</xsl:call-template>
															</xsl:with-param>
														</xsl:call-template>
													</xsl:with-param>
												</xsl:call-template>
											</xsl:with-param>
										</xsl:call-template>
									</xsl:with-param>
								</xsl:call-template>
							</xsl:with-param>
						</xsl:call-template>
					</xsl:with-param>
				</xsl:call-template>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>

	<xsl:template name="esc">
		<xsl:param name="s"/>
		<xsl:param name="c"/>

		<xsl:choose>
			<xsl:when test="contains($s, $c)">
				<xsl:value-of select="substring-before($s, $c)"/>
				<xsl:choose>
					<xsl:when test="$c = '\'">
						<xsl:text>\textbackslash </xsl:text>
					</xsl:when>
					
					<xsl:when test="$c = '&#160;'">
						<xsl:text> </xsl:text>
					</xsl:when>
					
					<xsl:when test="$c = '&quot;'">
						<xsl:text>&quot;&quot; </xsl:text>
					</xsl:when>
					
					<xsl:otherwise>
							<xsl:text>\</xsl:text>
						<xsl:value-of select="$c"/>
					</xsl:otherwise>
				</xsl:choose>

				<xsl:call-template name="esc">
					<xsl:with-param name="c" select="$c"/>
					<xsl:with-param name="s" select="substring-after($s, $c)"/>
				</xsl:call-template>
			</xsl:when>
			
			<xsl:otherwise>
				<xsl:value-of select="$s"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- End of LaTeXChar template -->


	<!-- XHTML -->
	<xsl:template match="html">
		<xsl:apply-templates select="body"/>
	</xsl:template>

	<!-- body sections -->
	<xsl:template match="h1">
		<xsl:text>\section{</xsl:text>
		<xsl:apply-templates />
		<xsl:text>}
</xsl:text>
	</xsl:template>

	<xsl:template match="h2">
		<xsl:text>\subsection{</xsl:text>
		<xsl:apply-templates />
		<xsl:text>}
</xsl:text>
	</xsl:template>

	<xsl:template match="h3">
		<xsl:text>\subsubsection{</xsl:text>
		<xsl:apply-templates />
		<xsl:text>}
</xsl:text>
	</xsl:template>

	<!-- section labels. -->
	<!-- lists -->
	<xsl:template match="ul">
		<xsl:text>\begin{itemize}
</xsl:text>
		<xsl:for-each select="li">
			<xsl:text>\item </xsl:text>
			<xsl:apply-templates />
		</xsl:for-each>
		<xsl:text>\end{itemize}
</xsl:text>
	</xsl:template>

	<xsl:template match="ol">
		<xsl:text>\begin{enumerate}
</xsl:text>
		<xsl:for-each select="li">
			<xsl:text>\item </xsl:text>
			<xsl:apply-templates />
		</xsl:for-each>
		<xsl:text>
\end{enumerate}
</xsl:text>
	</xsl:template>

	<xsl:template match="dl">
		<xsl:text>\begin{description}
</xsl:text>
		<xsl:for-each select="*">
			<xsl:if test="local-name() = 'dt'">
				<xsl:text>\item[</xsl:text>
			</xsl:if>
			<xsl:apply-templates />
			<xsl:if test="local-name() = 'dt'">
				<xsl:text>] </xsl:text>
			</xsl:if>
		</xsl:for-each>
		<xsl:text>
\end{description}
</xsl:text>
	</xsl:template>

	<!-- tables -->
	<xsl:template match="table">
		<xsl:text>\begin{center}
\begin{tabular}{|</xsl:text>
		<xsl:for-each select="tr[1]/*">
			<xsl:text>c|</xsl:text>
		</xsl:for-each>
		<xsl:text>}
</xsl:text>

		<xsl:for-each select="tr">
			<xsl:text>\hline
</xsl:text>
			<xsl:for-each select="*">
				<xsl:if test="name() = 'th'">{\bf </xsl:if>
				<xsl:apply-templates />
				<xsl:if test="name() = 'th'">}</xsl:if>
				<xsl:if test="position() != last()">
			<xsl:text> &amp; </xsl:text>
				</xsl:if>
			</xsl:for-each>
			<xsl:text> \\
</xsl:text>
		</xsl:for-each>
		<xsl:text>\hline
</xsl:text>

		<xsl:text> \end{tabular}
\end{center}
</xsl:text>
	</xsl:template>

	<!-- ol, img code untested -->
	<xsl:template match="img[@class = 'graphics'
						or @class = 'includegraphics']">
		<xsl:text>\includegraphics[width=</xsl:text>
		<xsl:value-of select="@width"/>
		<xsl:text>,height=</xsl:text>
		<xsl:value-of select="@height"/>
		<xsl:text>]{</xsl:text>
		<xsl:value-of select="@src"/>
		<xsl:text>}
</xsl:text>
	</xsl:template>


	<!-- blockquote -->
	<xsl:template match="blockquote">
		<xsl:text>\begin{quote}
</xsl:text>
		<xsl:apply-templates />
		<xsl:text>\end{quote}
</xsl:text>
	</xsl:template>

	<!-- misc pre/verbatim -->
	<xsl:template match="pre">
		<xsl:text>\begin{verbatim}
</xsl:text>
		<xsl:apply-templates mode="verbatim"/>
		<xsl:text>\end{verbatim}
</xsl:text>
	</xsl:template>


	<!-- paragraphs -->

	<xsl:template match="br">
		<xsl:text>

</xsl:text>
	</xsl:template>

	<xsl:template match="p">
		<xsl:choose>
			<xsl:when test="string(.) != ''">
				<xsl:apply-templates />
				<xsl:text>

</xsl:text>
			</xsl:when>
		</xsl:choose>
	</xsl:template>

	<!-- phrase markup -->

	<xsl:template match="em|dfn">
		<xsl:text>{\em </xsl:text>
		<xsl:apply-templates/>
		<xsl:text>}</xsl:text>
	</xsl:template>

	<xsl:template match="code">
		<xsl:text>{\tt </xsl:text>
		<xsl:apply-templates/>
		<xsl:text>}</xsl:text>
	</xsl:template>

	<xsl:template match="tt">
		<xsl:text>{\tt </xsl:text>
		<xsl:apply-templates/>
		<xsl:text>}</xsl:text>
	</xsl:template>

	<xsl:template match="i">
		<xsl:text>{\it </xsl:text>
		<xsl:apply-templates/>
		<xsl:text>}</xsl:text>
	</xsl:template>

	<xsl:template match="b">
		<xsl:text>{\bf </xsl:text>
		<xsl:apply-templates/>
		<xsl:text>}</xsl:text>
	</xsl:template>

	<xsl:template match="q">
		<xsl:text>``</xsl:text>
		<xsl:apply-templates />
		<xsl:text>''</xsl:text>
	</xsl:template>

	<xsl:template match="samp">
		<!-- pass-thru, for \Sigma -->
		<xsl:text>$</xsl:text>
		<xsl:value-of select="."/>
		<xsl:text>$</xsl:text>
	</xsl:template>

	<xsl:template match="samp" mode="math">
		<!-- pass-thru, for \Sigma -->
		<xsl:value-of select="."/>
	</xsl:template>

</xsl:stylesheet>

