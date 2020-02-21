<#macro generateBody taskDefinitions>

	/**
	 * Creates a taskBuilder.
	 * @param name  the name of the task
	 * @return the builder 
	 */
	private static TaskBuilder createTaskBuilder(final String name) {
		final TaskDefinition taskDefinition = Home.getApp().getDefinitionSpace().resolve(name, TaskDefinition.class);
		return Task.builder(taskDefinition);
	}

<#list taskDefinitions as taskDefinition>
	/**
	 * Execute la tache ${taskDefinition.name}.
	<#list taskDefinition.inAttributes as taskAttribute>
	 * @param ${taskAttribute.variableName} ${taskAttribute.javaTypeLabel} <#if !taskAttribute.required>(peut être null)</#if>
	</#list>
	<#if taskDefinition.out>
	 * @return <#if !taskDefinition.outAttribute.required>Option de </#if>${taskDefinition.outAttribute.javaTypeLabel} ${taskDefinition.outAttribute.variableName}
	</#if>
	*/
	public <#if taskDefinition.out><#if !taskDefinition.outAttribute.required>Optional<</#if>${taskDefinition.outAttribute.dataType}<#if !taskDefinition.outAttribute.required>></#if><#else>void</#if> ${taskDefinition.methodName}(<#list taskDefinition.inAttributes as taskAttribute>final <#if !taskAttribute.required>Optional<</#if>${taskAttribute.dataType}<#if !taskAttribute.required>></#if> ${taskAttribute.variableName}<#if taskAttribute_has_next>, </#if></#list>) {
		final Task task = createTaskBuilder("${taskDefinition.name}")
	<#list taskDefinition.inAttributes as taskAttribute>
				.addValue("${taskAttribute.name}", ${taskAttribute.variableName}<#if !taskAttribute.required>.orElse(null)</#if>)
	</#list>
				.build();
	<#if taskDefinition.out>
		<#if !taskDefinition.outAttribute.required>
		return Optional.ofNullable((${taskDefinition.outAttribute.dataType}) getTaskManager()
				.execute(task)
				.getResult());
		<#else>
		return getTaskManager()
				.execute(task)
				.getResult();
		</#if>
	 <#else>
		getTaskManager().execute(task);
    </#if>
	}

</#list>
</#macro>
