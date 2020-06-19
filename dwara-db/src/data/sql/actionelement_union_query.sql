select 
actionelement.id,
actionelement.active,
actionelement.display_order,
actionelement.encryption,
`action`.name as `action`,
lc.name as artclass,
storagetask.name as stask,
actionelement.actionelement_ref_id,
out_lc.name as out_artclass,
volume.uid as volume
from actionelement 
join `action` on action_id = `action`.id 
join artifactclass as lc on artifactclass_id = lc.id 
join storagetask on actionelement.storagetask_id = storagetask.id
left join artifactclass as out_lc on output_artifactclass_id = out_lc.id 
join volume on volume_id = volume.id

union all

select 
actionelement.id,
actionelement.active,
actionelement.display_order,
actionelement.encryption,
`action`.name as `action`,
lc.name as artclass,
processingtask.name as ptask,
actionelement.actionelement_ref_id,
out_lc.name as out_artclass,
volume.uid as volume
from actionelement 
join `action` on action_id = `action`.id 
join artifactclass as lc on artifactclass_id = lc.id 
join processingtask on actionelement.processingtask_id = processingtask.id
left join artifactclass as out_lc on output_artifactclass_id = out_lc.id 
join volume on volume_id = volume.id