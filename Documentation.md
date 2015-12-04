

# Introduction #
TODO

# The Document Adaptation Language (DALE) #
In this section we explain the HyperAdapt aspect language. The following UML like diagram gives a high-level overview of the concepts of the Document Adaptation LanguagE (DALE). At the most abstract level, adaptation objectives can be arranged in  _adaptation concerns_ like device independence, language independence etc. . In DALE, each concern manifestates as a set of _adaptation aspect_ modules. Each module consists of an ordered set of _advice actions_---the actual adaptation rules. DALE provides a set of specific kinds of advice actions, for example for enriching or reducing XML nodes, or to model variants in XML content. A detailed description can be found in the section on [advice actions](Documentation#Advice_Actions_(Adaptation_Rules).md).

![http://hyperadapt.googlecode.com/svn/wiki/figures/taxonomy.png](http://hyperadapt.googlecode.com/svn/wiki/figures/taxonomy.png)

Advice actions can be grouped in _advice groups_. Each group matches a certain _programmatic pointcut_. In aspect oriented programming, a pointcut expression matches a set of states (_joinpoints_) in programm execution which are augmented by advice (i.e., blocks of code). In HyperAdapt, we call these kinds of points _programmatic joinpoints_, which are actually points in execution of XML transformations. Aa explained [here](Documentation#Integration_into_Transformation_Engines.md), the programmatic joinpoint model and PX-Weave adapted to abitrary XML transformation architectures. Furthermore, DALE supports content-based pointcuts which are expressed by xPath expressions. A content-based pointcut selects parts (e.g., nodes or attributes) of the document under transformation that should be adapted by advice.

Besides pointcuts, an adaptation aspects advice depends on context parameters. These parameters have to be provided by the context model, which holds data collected by sensors on client-side. Typically, the context model contains information about the device that sends the request (e.g., screen resolution, browser etc.) and the user profile (e.g., including a users language, age etc.). Currently, only a simply key value mapping of context parameters is supported which have to be declared in an aspects interface section (see the XML schema below).


## DALE XML Schema ##

```
<?xml version="1.0" encoding="UTF-8"?>
<schema targetNamespace="http://www.hyperadapt.net/pxweave/aspects">
<element name="aspect" type ="tns:Aspect"/>
    <complexType name="Aspect">
	<sequence>
	    <element 
                name="interface" 
                type="tns:Interface"/>
	    <element 
                name="adviceGroup" 
                minOccurs="1" 
                maxOccurs="unbounded" 
                type="tns:AdviceGroup"/>
	</sequence>
    	<attribute 
            name="name" 
            type="string"/>
    </complexType>
<!-- further definitions -->
</schema>
```

## Joinpoint Model ##

## Advice Actions (Adaptation Rules) ##

### Kinds of Advice ###
Our aspect language provides three kinds of advice actions which will be explained in this section.

**BasicAdvice** is the common ancestor of all adaptation rule types in our aspect language. It just states that an advice can select a set of content-based joinpoints (i.e., nodes in the document) by an xPath expression.
```
<complexType name="BasicAdvice" abstract="true">
    <sequence>
	<element name="pointcut" type ="tns:XPath"/>
    </sequence>
</complexType>
```

**SimpleAdvice** actions are used to manipulate simple content in an XML document, e.g., inserting or deleting text and changing attribute values. Its value can therefore be general text or restricted text content (e.g., a number).
```
<complexType name="SimpleAdvice" abstract="true">
    <complexContent>
	<extension base="tns:BasicAdvice">
	   <sequence>
             <element name="value" type="tns:SimpleValue"/>
	   </sequence>
	</extension>
    </complexContent>
</complexType>
```

In contrast to the above mentioned kinds of advice, **ComplexAdvice** may contain abitrary, well-formed XML content or an XML tree.
```
<complexType name="ComplexAdvice" abstract="true">
    <complexContent>
	<extension base="tns:BasicAdvice">
	    <sequence>
	        <element name="value" type="tns:ComplexValue"/>
	    </sequence>
        </extension>
    </complexContent>
</complexType>
```

### Basic Advice Actions ###
#### ChangeOrder ####
This advice allows a re-arrangement of the child nodes of an element within the set of selected joinpoints. There are three options. _sortByName_ computes an ascending, alphanumerical order of child nodes with respect to a common `name` attribute (NOTE: The order determining attribute should be selectable, as well as the kind of order itself.) Second, _reverseOrder_ just inverts the order of child nodes. Finally, a permutation can be specified, which allows an arbitrary, user-specified re-arrangement.
```
<complexType name="ChangeOrder">
   <complexContent>
      <extension base="tns:BasicAdvice">			
         <choice>
	    <element name="sortByName"/><!--by attr-->
	    <element name="reverseOrder"/>
	    <element name="permutation" type="tns:Permutation"/>
         </choice>
      </extension>
    </complexContent>
</complexType>
```

#### ChooseVariant ####
The **ChooseVariant** advice selects a variant from a list of exchangeable nodes.
```
<complexType name="ChooseVariant">
   <complexContent>
       <extension base="tns:BasicAdvice"/>
   </complexContent>
</complexType>
```
To be applicable, a certain node pattern is required, e.g.:
<table>
<tr>
<th>
Input Pattern<br>
</th>
<th>
Advice<br>
</th>
<th>
Result<br>
</th>
</tr>
<tr>
<td>
<pre><code>&lt;root&gt;<br>
   &lt;a&gt;<br>
      &lt;x/&gt;<br>
      &lt;y/&gt;<br>
      &lt;z/&gt;<br>
   &lt;/a&gt;<br>
&lt;/root&gt;<br>
</code></pre>
</td>
<td height='100%'>
<pre><code>&lt;chooseVariant&gt;<br>
   &lt;pointcut&gt;/root/a/z&lt;/pointcut&gt;<br>
&lt;/chooseVariant&gt;<br>
</code></pre>
</td>
<td>
<pre><code>&lt;root&gt;<br>
   &lt;z/&gt;<br>
&lt;/root&gt;<br>
</code></pre>
</td>
</tr>
</table>

#### Delete ####
This is a basic advice for deleting nodes in a document. The kind of deleted node is determined by the specified pointcut expression which may select element, attributes or text nodes.
```
<complexType name="Delete">
   <complexContent>
      <extension base="tns:BasicAdvice"/>
   </complexContent>
</complexType>
```

#### MoveElement ####
This advice moves selected element nodes to a new position within the document it is applied to. The source element(s) are specified by the specified pointcut xPath expression while the target is specfied by an xPath expression in the _to_ element. Position is an additional child index position at the target position.
```
<complexType name="MoveElement">
   <complexContent>
      <extension base="tns:BasicAdvice">
	 <sequence>
	    <element name="to" type="tns:XPath"/>		
	 </sequence>	
	 <attribute name= "position" type="integer" use="required"/>
      </extension>
   </complexContent>
</complexType>
```


#### ReduceContent ####
This advice removes parts of the content of a text node.
```
<complexType name="ReduceContent">
   <complexContent>
      <extension base="tns:BasicAdvice">
         <attribute name="deletePart" type="string"/>
      </extension>
   </complexContent>
</complexType>
```

#### !FillComponentByID ####
```
<complexType name="FillComponentByID">
    <complexContent>
        <extension base="tns:BasicAdvice">
 	    <sequence>
               <group ref="tns:componentGroup"/>
 	          <element name="source" type="tns:SourceContainer"/>
 	    </sequence>
        </extension>
    </complexContent>
</complexType>

<!-- Source Container Helper -->

<complexType name="SourceContainer">
   <sequence>
      <element name="sourceComponents" type="tns:XPath"/>
      <group name="componentGroup">
         <sequence>
	   <element name="identifyingAttribute" type="string"/>
	</sequence>
      </group>
   </sequence>
   <attribute name="document" type="tns:Name"/>
</complexType>
```

### Advice Actions with Simple Content ###

#### ChangeValue ####
```
<complexType name="ChangeValue">
   <complexContent>
      <extension base="tns:SimpleAdvice"/>
   </complexContent>
</complexType>
```

#### CollapseElement ####
```
<complexType name="CollapseElement">
   <complexContent>
      <extension base="tns:SimpleAdvice"/>
   </complexContent>
</complexType>
```

#### EnrichContent ####
```
<complexType name="EnrichContent">
   <complexContent>
      <extension base="tns:SimpleAdvice">
         <attribute name="position" type="integer"/>	
      </extension>
   </complexContent>
</complexType>
```


### Advice Actions with Complex Content ###

#### ExpandElement ####
```
<complexType name="ExpandElement">
   <complexContent>
      <extension base="tns:ComplexAdvice"/>
   </complexContent>
</complexType>
```

#### InsertElement ####
```
<complexType name="InsertElement">
   <complexContent>
      <extension base="tns:ComplexAdvice">
	<attribute name= "position" type="integer" use="required"/>	
      </extension>	
   </complexContent>
</complexType>
```

## What can be _adviced_? ##

## Examples ##

# PX-Weave Weaving Engine #

## Basic Configuration ##
PX-Weave can be configured via a weaver configuration file as shown below. A `weaverConfiguration` consists of three main parts.

  * **aspect-locations** Within the range of the `aspectFiles` element, each aspect file that should be considered by the weaving engine has to be declared by an URI pointing to its location. The URI can be relative or absolute. In the relative case, the URI will be resolved against the configuration files location.
  * **namespace-declarations** To allow the xPath expressions to select nodes appropriately w.r.t. their namespace prefixes, all namespaces that occur in target document can be declared within the `namespaces` element by specifying its prefix and URI.
  * **validation** PX-Weave supports several validation modes depending on the underlying XML parsing technology (by default, Apache Xcerces is used). By using the `validateBeforeWeaving` and `validateAfterWeaving` attributes it can be specified, if a schema-based validation should take place directly before or after a single weaving step. Validation can be disabled by setting validation mode to `none` and `validateBeforeWeaving` and `validateAfterWeaving` to false if no schema is available. The `DOML3` option enables validation before and after every weaving operation by checking the whole document against its schema. `DOML3Validation` uses the DOM level 3 validaiton API for local validation. This requires the xmlparserv2.jar from the Oracle 10g XDK, which must be downloaded seperately.

```
<?xml version="1.0" encoding="UTF-8" ?>
<weaverConfiguration 
  xsi:schemaLocation="http://www.hyperadapt.org/aspects schemas/weaverConfig.xsd" 
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns="http://www.hyperadapt.org/aspects">
    <aspectFiles>
       <aspectFile>path/to/aspect.xml</aspectFile>
       <!-- add further aspects files here -->
    </aspectFiles>
    <namespaces> 
       <namespaceURI prefix="prefix">
           http://name/space/uri
       </namespaceURI>
       <!-- add further XML namespace declarations here -->
    </namespaces>
    <validationMode 
        validateBeforeWeaving="false|true"
        validateAfterWeaving="false|true">
          none <!--none, DOML3, DOML3Validation -->
   </validationMode>
</weaverConfiguration>

```

## Implementation of the Joinpoint Model ##

## Integration into Transformation Engines ##

![http://hyperadapt.googlecode.com/svn/wiki/figures/environment.png](http://hyperadapt.googlecode.com/svn/wiki/figures/environment.png)

### Integration Concept ###

### Cocoon Integration ###

# Aspect Interactions and Conflict Detection #

## Kinds of Conflicts ##

## Detection and Reporting at Runtime ##

## Approximated Static Conflict Detection ##